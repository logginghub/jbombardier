/*
 * Copyright (c) 2009-2015 Vertex Labs Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jbombardier.console.components;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.logginghub.utils.AbstractBean;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableDouble;
import com.logginghub.utils.observable.ObservableItemContainer;
import com.logginghub.utils.observable.ObservableListener;

public class ReflectiveTable<T> extends JTable {
    public interface SelectionHandler<T> {
        void onSelected(T item);
    }

    private int maximumEntries = -1;
    private static final long serialVersionUID = 1L;
    private List<T> visibleEntries = new ArrayList<T>();
    private List<T> hiddenEntries = new ArrayList<T>();
    private ReflectiveTableModel model = new ReflectiveTableModel();
    private ArrayList<Method> accessors = new ArrayList<Method>();
    private Map<String, Method> setters = new HashMap<String, Method>();
    private List<ChangeHandler> changeHandlers = new CopyOnWriteArrayList<ReflectiveTable.ChangeHandler>();
    private Set<String> hiddenColumnNames = new HashSet<String>();
    private Class<T> classToReflect;
    private Map<String, String> columnNameReplacements = new HashMap<String, String>();
    private RowVisibilityFilter<T> rowVisibilityFilter;
    private String[] columnOrder;

    private TableDataProvider<T> dataProvider = null;

    public interface RowVisibilityFilter<T> {
        boolean isVisible(T row);
    }

    public interface ChangeHandler<T> {
        void onChange(T entry, String field, Object newValue);
    }

    public ReflectiveTable(Class<T> c) {
        init(c);
    }

    private void init(Class<T> c) {
        updateReflectionModel(c);
        setModel(model);
        setColumnSelectionAllowed(false);
        setCellSelectionEnabled(false);
        setRowSelectionAllowed(true);
        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public ReflectiveTable(Class<T> c, TableDataProvider<T> provider) {
        this.dataProvider = provider;
        init(c);
    }

    public void setTableDataProvider(TableDataProvider<T> dataProvider) {
        this.dataProvider = dataProvider;
    }

    // @Override public Component prepareRenderer(TableCellRenderer renderer,
    // int rowIndex, int vColIndex) {
    //
    // Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
    // if (rowIndex % 2 == 0 && !isCellSelected(rowIndex, vColIndex)) {
    // c.setForeground(Color.yellow);
    // }
    // else {
    // // If not shaded, match the table's background
    // c.setForeground(Color.GREEN);
    // }
    // return c;
    // }

    private void updateReflectionModel(Class<T> c) {
        this.classToReflect = c;

        accessors.clear();
        setters.clear();

        if (columnOrder != null) {
            for (int i = 0; i < columnOrder.length; i++) {
                accessors.add(null);
            }
        }

        Method[] methods = c.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().startsWith("get") || method.getName().startsWith("is")) {
                if (method.getParameterTypes().length == 0) {

                    String removeAccessorPart = removeAccessorPart(method.getName());

                    if (hiddenColumnNames.contains(removeAccessorPart.toLowerCase())) {
                        // Not displaying this column
                    }
                    else {
                        // Looks like a goer
                        if (columnOrder != null) {
                            int indexOf = Arrays.asList(columnOrder).indexOf(removeAccessorPart);
                            if (indexOf == -1) {
                                throw new RuntimeException("You've missed a column ('" + removeAccessorPart + "') from the list of ordered columns...");
                            }

                            accessors.set(indexOf, method);
                        }
                        else {
                            accessors.add(method);
                        }

                        try {
                            Method setter = c.getMethod("set" + removeAccessorPart, method.getReturnType());
                            setters.put(removeAccessorPart, setter);
                        }
                        catch (SecurityException e) {}
                        catch (NoSuchMethodException e) {}
                    }
                }
            }
        }
    }

    private class ReflectiveTableModel extends DefaultTableModel {
        private static final long serialVersionUID = 1L;
        private RowVisibilityFilter<T> rowVisibilityFilter;
        private Comparator<T> comparator;
        private boolean editable = false;

        @Override public int getColumnCount() {
            if (dataProvider != null) {
                return dataProvider.getColumnCount();
            }
            else {
                return accessors.size();
            }
        }

        @Override public int getRowCount() {
            synchronized (visibleEntries) {
                return visibleEntries.size();
            }
        }

        @Override public boolean isCellEditable(int row, int column) {
            return editable;
        }

        @Override public void setValueAt(Object aValue, int row, int column) {

            T entry;
            synchronized (visibleEntries) {
                entry = visibleEntries.get(row);
            }

            Method method = accessors.get(column);
            String attribute = removeAccessorPart(method.getName());

            for (ChangeHandler changeHandler : changeHandlers) {

                Class<?> returnType = method.getReturnType();

                try {
                    Object converted = convert(aValue, returnType);
                    changeHandler.onChange(entry, attribute, converted);
                }
                catch (NumberFormatException nfe) {

                }
            }

            // Method setter = setters.get(attribute);
            // try {
            // setter.invoke(entry, convert(aValue,
            // setter.getParameterTypes()[0]));
            // }
            // catch (Exception e) {
            // e.printStackTrace();
            // }
        }

        @Override public Object getValueAt(int row, int column) {
            Object value;

            T entry = getItemAtRow(row);

            if (dataProvider != null) {
                value = dataProvider.getValueForColumn(column, entry);
            }
            else {
                Method method = accessors.get(column);
                try {
                    value = method.invoke(entry, (Object[]) null);
                }
                catch (Exception e) {
                    value = method.getName() + " failed : " + e.getMessage();
                }

                if (value instanceof Float || value instanceof Double) {
                    value = NumberFormat.getInstance().format(value);
                }
                else if (value instanceof ObservableDouble) {
                    ObservableDouble observableDouble = (ObservableDouble) value;
                    value = NumberFormat.getInstance().format(observableDouble.doubleValue());
                }
            }

            return value;
        }

        @Override public String getColumnName(int column) {
            String columnName;
            if (dataProvider != null) {
                columnName = dataProvider.getColumnName(column);
            }
            else {
                Method method = accessors.get(column);
                String name = method.getName();
                columnName = removeAccessorPart(name);
            }

            String replacement = columnNameReplacements.get(columnName);
            if (replacement != null) {
                columnName = replacement;
            }

            return columnName;
        }

        public void addItem(final T item) {

            int index;
            int removed = -1;
            synchronized (visibleEntries) {
                index = visibleEntries.size();
                visibleEntries.add(item);

                if (maximumEntries != -1 && visibleEntries.size() > maximumEntries) {
                    removed = 0;
                    visibleEntries.remove(0);
                    index--;
                }
            }

            notifyAndUpdate(item, index, removed);
        }

        public void addItemReverse(final T item) {

            int index;
            int removed = -1;
            synchronized (visibleEntries) {
                index = 0;
                visibleEntries.add(index, item);

                if (maximumEntries != -1 && visibleEntries.size() > maximumEntries) {
                    removed = visibleEntries.size() - 1;
                    visibleEntries.remove(removed);
                }
            }

            notifyAndUpdate(item, index, removed);
        }

        private void notifyAndUpdate(final T item, int index, int removed) {
            if (removed != -1) {
                fireTableRowsDeleted(removed, removed);
            }

            fireTableRowsInserted(index, index);

            if (item instanceof AbstractBean) {
                AbstractBean abstractBean = (AbstractBean) item;
                abstractBean.addPropertyChangeListener(new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        updateFromItem(item);
                    }
                });
            }
            else if (item instanceof Observable) {
                Observable observable = (Observable) item;
                observable.addListener(new ObservableListener() {
                    @Override public void onChanged(ObservableItemContainer observable, Object childPropertyThatChanged) {
                        updateFromItem(item);
                    }
                });
            }
        }

        private void updateFromItem(final T item) {
            int rowIndex;
            synchronized (visibleEntries) {
                rowIndex = visibleEntries.indexOf(item);
            }

            // The row might have been in the hidden entries list
            if (rowIndex >= 0) {
                fireTableRowsUpdated(rowIndex, rowIndex);
            }
        }

        public void clear() {
            synchronized (visibleEntries) {
                visibleEntries.clear();
            }
            fireTableDataChanged();
        }

        public void setRowVisibilityFilter(final RowVisibilityFilter<T> rowVisibilityFilter) {
            this.rowVisibilityFilter = rowVisibilityFilter;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    synchronized (visibleEntries) {

                        Iterator<T> iterator = visibleEntries.iterator();
                        while (iterator.hasNext()) {
                            T t = iterator.next();
                            if (rowVisibilityFilter.isVisible(t)) {
                                // Fine, leave it
                            }
                            else {
                                hiddenEntries.add(t);
                                iterator.remove();
                            }
                        }
                    }
                    fireTableDataChanged();
                }
            });

        }

        public void clearRowVisibilityFilter() {
            rowVisibilityFilter = null;

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    visibleEntries.addAll(hiddenEntries);
                    sort();
                    hiddenEntries.clear();
                    fireTableDataChanged();
                }
            });

        }

        public void sort() {
            if (comparator != null) {
                Collections.sort(visibleEntries, comparator);
            }
        }

        public void setRowComparator(Comparator<T> comparator) {
            this.comparator = comparator;
        }

        public void setEditable(boolean value) {
            editable = value;
        }

        public T getItemAtRow(int row) {
            T entry;
            synchronized (visibleEntries) {
                entry = visibleEntries.get(row);
            }
            return entry;
        }
    }

    public void addItem(T item) {
        this.model.addItem(item);
    }

    public void addItemReverse(T item) {
        this.model.addItemReverse(item);
    }

    public T getItemAtRow(int rowIndex) {
        return model.getItemAtRow(rowIndex);
    }

    public Object convert(Object aValue, Class<?> class1) {

        Object converted;
        if (class1 == Integer.TYPE) {
            converted = Integer.parseInt(aValue.toString());
        }
        else if (class1 == Float.TYPE) {
            converted = Float.parseFloat(aValue.toString());
        }
        else if (class1 == Double.TYPE) {
            converted = Double.parseDouble(aValue.toString());
        }
        else if (class1 == Short.TYPE) {
            converted = Short.parseShort(aValue.toString());
        }
        else if (class1 == Long.TYPE) {
            converted = Long.parseLong(aValue.toString());
        }
        else if (class1 == Byte.TYPE) {
            converted = Byte.parseByte(aValue.toString());
        }
        else {
            converted = aValue;
        }

        return converted;

    }

    public String removeAccessorPart(String name) {
        String sorted;
        if (name.startsWith("get")) {
            sorted = name.substring(3, name.length());
        }
        else if (name.startsWith("is")) {
            sorted = name.substring(2, name.length());
        }
        else {
            sorted = name;
        }

        return sorted;
    }

    public void addChangeHandler(ChangeHandler<T> changeHandler) {
        changeHandlers.add(changeHandler);
    }

    public void removeChangeHandler(ChangeHandler<T> changeHandler) {
        changeHandlers.remove(changeHandler);
    }

    public void clear() {
        model.clear();
    }

    public void setHiddenColumns(String... columnNames) {
        dontShowColumns(columnNames);
    }

    public void dontShowColumns(String... columnNames) {
        for (String string : columnNames) {
            hiddenColumnNames.add(string.toLowerCase());
        }

        updateReflectionModel(classToReflect);
        model.fireTableStructureChanged();
    }

    public void setColumnIndex(String string, int index) {

        Method found = null;
        for (Method method : accessors) {
            if (removeAccessorPart(method.getName()).equals(string)) {
                found = method;
                break;
            }
        }

        if (found != null) {
            accessors.remove(found);
            accessors.add(index, found);
        }

        model.fireTableStructureChanged();
    }

    public void setColumnNameReplacement(String original, String newName) {
        columnNameReplacements.put(original, newName);
        model.fireTableStructureChanged();
    }

    public void setRowVisibilityFilter(RowVisibilityFilter<T> rowVisibilityFilter) {
        model.setRowVisibilityFilter(rowVisibilityFilter);
    }

    public void clearRowVisibilityFilter() {
        model.clearRowVisibilityFilter();
    }

    public void setRowComparator(Comparator<T> comparator) {
        model.setRowComparator(comparator);
    }

    public void setColumnOrder(final String... names) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                columnOrder = names;
                updateReflectionModel(classToReflect);
                model.fireTableStructureChanged();
            }
        });
    }

    public void setEditable(boolean value) {
        model.setEditable(value);
    }

    public void addSelectionListener(final SelectionHandler<T> selectionHandler) {
        this.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                ListSelectionModel lsm = (ListSelectionModel) e.getSource();

                if (!lsm.isSelectionEmpty()) {
                    // Find out which indexes are selected.
                    int minIndex = lsm.getMinSelectionIndex();
                    int maxIndex = lsm.getMaxSelectionIndex();
                    for (int i = minIndex; i <= maxIndex; i++) {
                        if (lsm.isSelectedIndex(i)) {
                            T itemAtRow = model.getItemAtRow(i);
                            selectionHandler.onSelected(itemAtRow);
                        }
                    }
                }
            }
        });
    }

    public void setMaximumEntries(int maximumEntries) {
        this.maximumEntries = maximumEntries;
    }

    public void forceColumnWidth(int columnIndex, int width) {
        TableColumn column = getColumnModel().getColumn(columnIndex);
        column.setPreferredWidth(width);
        column.setWidth(width);
        column.setMinWidth(width);
    }
}
