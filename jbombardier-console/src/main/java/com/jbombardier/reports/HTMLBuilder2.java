/*
 * Copyright (c) 2009-2016 Vertex Labs Limited.
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

package com.jbombardier.reports;

import com.logginghub.utils.FileUtils;
import com.logginghub.utils.ResourceUtils;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.StringUtils.StringUtilsBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Improved HTMLBuilder - uses a structured tree of elements to make building stuff up easier
 *
 * @author James
 */
public class HTMLBuilder2 {

    private Element html = new Element("html");
    private HeadElement head = new HeadElement();
    private Element body;
    private String docType;
    private List<String> associatedResources = new ArrayList<String>();

    private final static Set<String> prettyInlineTags = new HashSet<String>();

    public HTMLBuilder2() {
        html.children.add(head);
        body = html.createChild("body");

        prettyInlineTags.add("a");
        prettyInlineTags.add("td");
    }

    public String toString() {
        StringUtilsBuilder builder = new StringUtilsBuilder();
        if (StringUtils.isNotNullOrEmpty(docType)) {
            builder.appendLine(docType);
        }
        html.toString(builder);
        return builder.toString();
    }

    public String toStringPretty() {
        StringUtilsBuilder builder = new StringUtilsBuilder();
        if (StringUtils.isNotNullOrEmpty(docType)) {
            builder.appendLine(docType);
        }
        html.toString(builder, 0, true);
        return builder.toString();
    }

    public static String htmlify(String text) {
        String htmlified = text.replace(" ", "&nbsp;");
        return htmlified;
    }

    public HeadElement getHead() {
        return head;
    }

    public Element getBody() {
        return body;
    }

    public Element getHtml() {
        return html;
    }

    public void toFile(File file) {
        FileUtils.write(toString(), file);
    }

    public void toFilePretty(File file) {
        FileUtils.write(toStringPretty(), file);
    }

    public void copyAssociatedResourcesTo(File folder) {
        try {
            for (String associatedResource : associatedResources) {
                String name = StringUtils.afterLast(associatedResource, "/");
                File output = new File(folder, name);
                FileUtils.write(ResourceUtils.openStream(associatedResource), output);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public void associateResource(String s) {
        associatedResources.add(s);
    }

    public static class TableElement extends Element {

        private THeadElement thead;
        private Element tbody;

        public TableElement() {
            super("table");

            thead = new THeadElement();
            tbody = new Element("tbody");

            children.add(thead);
            children.add(tbody);
        }

        public Element getTbody() {
            return tbody;
        }

        public THeadElement getThead() {
            return thead;
        }

    }

    public static class THeadElement extends Element {

        public THeadElement() {
            super("thead");
        }

        public THeadElement cells(String... text) {
            for (String s : text) {
                Element child = new Element("th");
                child.setText(s);
                addChild(child);
            }

            return this;
        }

        public THeadRow headerRow() {
            THeadRow element = new THeadRow();
            addChild(element);
            return element;
        }

    }

    public static class THeadRow extends Element {

        public THeadRow() {
            super("tr");
        }

        public THeadRow cells(String... text) {
            for (String s : text) {
                Element child = new Element("th");
                child.setText(s);
                addChild(child);
            }

            return this;
        }

        public Element cell(String text) {
            Element child = new Element("th");
            child.setText(text);
            addChild(child);
            return child;
        }

    }


    public static class RowElement extends Element {

        public RowElement() {
            super("tr");
        }

        public Element cell(Object object) {
            Element child = new Element("td");
            child.setText(object.toString());
            addChild(child);
            return child;
        }

        public Element cell() {
            Element child = new Element("td");
            addChild(child);
            return child;
        }

        public Element cell(String format, Object... params) {
            Element child = new Element("td");
            child.setText(format, params);
            addChild(child);
            return child;
        }

        public RowElement cells(String... text) {
            for (String s : text) {
                Element child = new Element("td");
                child.setText(s);
                addChild(child);
            }

            return this;
        }
    }

    public static class HeadElement extends Element {
        public HeadElement() {
            super("head");
        }

        public HeadElement css(String href) {
            Element child = new Element("link");
            child.setAttribute("rel", "stylesheet");
            child.setAttribute("type", "text/css");
            child.setAttribute("href", href);
            children.add(child);
            return this;
        }

        public HeadElement addStyleSheet(String href) {
            return css(href);
        }

        public HeadElement addJavascript(String href) {
            Element child = new Element("script");
            child.setAttribute("type", "text/javascript");
            child.setAttribute("src", href);
            children.add(child);
            return this;
        }

    }

    public static class Element {

        public final static String text_tag = null;
        private String tag;
        private Element parent;
        protected List<Element> children = new ArrayList();
        private String text;
        private Map<String, String> attributes = new HashMap<String, String>();

        public Element(String tag) {
            this.tag = tag;
        }

        public Element(String tag, String text) {
            this.tag = tag;
            this.text = text;
        }

        public String getTag() {
            return tag;
        }

        public Element createChild(String tag) {
            Element child = new Element(tag);
            children.add(child);
            return child;
        }

        public Element id(String s) {
            attributes.put("id", s);
            return this;
        }

        public void addChild(Element e) {
            children.add(e);
        }

        public void setAttribute(String key, String value, Object... params) {
            attributes.put(key, StringUtils.format(value, params));
        }

        public void setAttribute(String key, Object value) {
            attributes.put(key, value.toString());
        }

        public String toString() {
            StringUtilsBuilder builder = new StringUtilsBuilder();
            toString(builder);
            return builder.toString();
        }

        public RowElement createRow() {
            RowElement element = new RowElement();
            addChild(element);
            return element;
        }

        public RowElement row() {
            return createRow();
        }

        public void toString(StringUtilsBuilder builder, int indent, boolean pretty) {

            if (tag != text_tag) {
                if (pretty) {
                    builder.append(StringUtils.repeat(" ", indent * 4));
                }
                builder.append("<").append(tag);
            }

            if (!attributes.isEmpty()) {
                String div = " ";
                Set<Entry<String, String>> entrySet = attributes.entrySet();
                for (Entry<String, String> entry : entrySet) {
                    builder.append(div).append(entry.getKey()).append("=\"").append(entry.getValue()).append("\"");
                }
            }

            if (tag != text_tag) {
                builder.append(">");
                if (pretty && !prettyInlineTags.contains(tag)) {
                    builder.newline();
                }
            }

            if (text != null) {
                if (pretty && !prettyInlineTags.contains(tag)) {
                    builder.append(StringUtils.repeat(" ", indent * 4));
                }
                builder.append(text);
                if (pretty && !prettyInlineTags.contains(tag)) {
                    builder.newline();
                }
            }

            for (Element child : children) {
                child.toString(builder, indent + 1, pretty);
            }

            if (tag != text_tag) {
                if (pretty && !prettyInlineTags.contains(tag)) {
                    builder.append(StringUtils.repeat(" ", indent * 4));
                }
                builder.append("</").append(tag).append(">");
                if (pretty) {
                    builder.newline();
                }
            }

        }

        public void toString(StringUtilsBuilder builder) {
            toString(builder, 0, false);
        }

        public void setText(String format, Object... objects) {
            this.text = StringUtils.format(format, objects);
        }

        public void setTextHtmlified(String format, Object... objects) {
            this.text = htmlify(StringUtils.format(format, objects));
        }

        public TableElement createTable() {
            TableElement child = new TableElement();
            children.add(child);
            return child;
        }

        public TableElement table() {
            return createTable();
        }

        public Element createImage(String string) {
            Element child = new Element("img");
            child.setAttribute("src", string);
            children.add(child);
            return child;
        }

        public Element image(String src) {
            return createImage(src);
        }

        public Element image(String src, int width, int height) {
            Element child = new Element("img");
            child.setAttribute("src", src);
            child.setAttribute("width", width);
            child.setAttribute("height", height);
            children.add(child);
            return child;
        }

        public Element divText(String format, Object... params) {
            return createChild("div", format, params);
        }

        public Element div(String id) {
            final Element div = createChild("div");
            div.setAttribute("id", id);
            return div;
        }

        public Element pre(String format, Object... params) {
            return createChild("pre", format, params);
        }

        public Element pre() {
            return createChild("pre");
        }

        private Element createChild(String tag, String format, Object[] params) {
            Element child = new Element(tag);
            child.setText(format, params);
            children.add(child);
            return child;
        }

        public Element li(String format, Object... params) {
            return createChild("li", format, params);
        }

        public Element h1(String format, Object... params) {
            return createChild("h1", format, params);
        }

        public Element h1() {
            return createChild("h1");
        }

        public Element h2(String format, Object... params) {
            return createChild("h2", format, params);
        }

        public Element h3(String format, Object... params) {
            return createChild("h3", format, params);
        }

        public Element h4(String format, Object... params) {
            return createChild("h4", format, params);
        }

        public Element h5(String format, Object... params) {
            return createChild("h5", format, params);
        }

        public Element h6(String format, Object... params) {
            return createChild("h6", format, params);
        }

        public Element input(String type, String value) {
            Element child = new Element("input");
            child.setAttribute("type", type);
            child.setAttribute("value", value);
            children.add(child);
            return child;
        }

        public Element input(String type, String name, String value) {
            Element child = new Element("input");
            child.setAttribute("type", type);
            child.setAttribute("name", name);
            child.setAttribute("value", value);
            children.add(child);
            return child;
        }

        public Element select(String name, String[] options) {
            Element child = new Element("select");
            child.setAttribute("name", name);

            for (String option : options) {
                Element optionChild = new Element("option");
                optionChild.setAttribute("value", option);
                optionChild.setText(option);
                child.children.add(optionChild);
            }

            children.add(child);
            return child;
        }


        public Element form(String name, String method, String action) {
            Element child = new Element("form");
            child.setAttribute("name", name);
            child.setAttribute("method", method);
            child.setAttribute("action", action);
            children.add(child);
            return child;
        }


        public Element div() {
            Element child = new Element("div");
            children.add(child);
            return child;
        }

        public Element span(String format, Object... params) {
            Element child = new Element("span");
            child.setText(format, params);
            children.add(child);
            return child;
        }

        public Element p(String format, Object... params) {
            Element child = new Element("p");
            child.setText(format, params);
            children.add(child);
            return child;
        }

        public Element br() {
            return createChild("br");
        }

        public Element a(String src, String format, Object... params) {
            Element child = new Element("a");
            child.setAttribute("href", src);
            child.setText(format, params);
            children.add(child);
            return child;
        }

        public boolean hasChildren() {
            return children.size() > 0;
        }

        public void addClass(String style) {

            String existingClasses = attributes.get("class");
            if (existingClasses != null) {
                String newClasses = existingClasses + " " + style;
                attributes.put("class", newClasses);
            } else {
                attributes.put("class", style);
            }

        }


        @Override protected void finalize() throws Throwable {
            super.finalize();
        }


        // public Element anchor(String name, String format, Object... params) {
        // Element child = new Element("a");
        // child.setAttribute("name", name);
        // child.setText(format, params);
        // children.add(child);
        // return child;
        // }

    }

    // private BufferedWriter writer;
    // private StringWriter stringWriter;
    //
    // public HTMLBuilder2(Writer writer) {
    // this.writer = new BufferedWriter(writer);
    // }
    //
    // public HTMLBuilder2() {
    // this.stringWriter = new StringWriter();
    // this.writer = new BufferedWriter(stringWriter);
    // }
    //
    // @Override public String toString() {
    // if (stringWriter != null) {
    // return stringWriter.toString();
    // }
    // else {
    // return this.toString();
    // }
    // }
    //
    // public void close() {
    // FileUtils.closeQuietly(writer);
    // }
    //
    // public HTMLBuilder2 table() {
    // appendLine("<table border='1'>");
    // return this;
    // }
    //
    // public HTMLBuilder2 tableNoBorder() {
    // appendLine("<table border='0'>");
    // return this;
    // }
    //
    // public HTMLBuilder2 endTable() {
    // appendLine("</table>");
    // return this;
    // }
    //
    // public HTMLBuilder2 html() {
    // appendLine("<html>");
    // return this;
    // }
    //
    // public HTMLBuilder2 endHtml() {
    // appendLine("</html>");
    // return this;
    // }
    //
    // public HTMLBuilder2 head() {
    // appendLine("<head>");
    // return this;
    // }
    //
    // public HTMLBuilder2 endHead() {
    // appendLine("</head>");
    // return this;
    // }
    //
    // public HTMLBuilder2 body() {
    // appendLine("<body>");
    // return this;
    // }
    //
    // public HTMLBuilder2 endBody() {
    // appendLine("</body>");
    // return this;
    // }
    //
    // public HTMLBuilder2 tr() {
    // appendLine("<tr>");
    // return this;
    // }
    //
    // public HTMLBuilder2 td() {
    // appendLine("<td>");
    // return this;
    // }
    //
    // public HTMLBuilder2 endTr() {
    // appendLine("</tr>");
    // return this;
    // }
    //
    // public HTMLBuilder2 endTd() {
    // appendLine("</td>");
    // return this;
    // }
    //
    // public HTMLBuilder2 th(Object content) {
    // appendLine("<th>" + content + "</th>");
    // return this;
    // }
    //
    // public HTMLBuilder2 endTh() {
    // appendLine("</th>");
    // return this;
    // }
    //
    // public HTMLBuilder2 tdFormat(double value) {
    // NumberFormat instance = NumberFormat.getInstance();
    // instance.setMaximumFractionDigits(2);
    // instance.setMinimumFractionDigits(2);
    // return td(instance.format(value));
    // }
    //
    // public HTMLBuilder2 td(Object value) {
    // appendLine("<td nowrap='nowrap'>" + value + "</td>");
    // return this;
    // }
    //
    // public HTMLBuilder2 td(String content, Object... values) {
    // appendLine("<td nowrap='nowrap'>" + StringUtils.format(content, values) + "</td>");
    // return this;
    // }
    //
    // public void appendLine(String string) {
    // try {
    // writer.append(string);
    // writer.newLine();
    // }
    // catch (IOException e) {
    // throw new RuntimeException(e);
    // }
    // }
    //
    // public HTMLBuilder2 appendLine(String format, Object... values) {
    // try {
    // writer.append(StringUtils.format(format, values));
    // writer.newLine();
    // }
    // catch (IOException e) {
    // throw new RuntimeException(e);
    // }
    // return this;
    // }
    //
    // public HTMLBuilder2 append(String format, Object... values) {
    // try {
    // writer.append(StringUtils.format(format, values));
    // }
    // catch (IOException e) {
    // throw new RuntimeException(e);
    // }
    // return this;
    // }
    //
    //
    // public HTMLBuilder2 thead() {
    // appendLine("<thead>");
    // return this;
    // }
    //
    // public HTMLBuilder2 endThead() {
    // appendLine("</thead>");
    // return this;
    // }
    //
    // public HTMLBuilder2 p() {
    // appendLine("<p>");
    // return this;
    // }
    //
    // public HTMLBuilder2 endP() {
    // appendLine("</p>");
    // return this;
    // }
    //
    // public HTMLBuilder2 div(String string, Object... params) {
    // appendLine("<div>" + string + "</div>", params);
    // return this;
    // }
    //
    // public HTMLBuilder2 span(String string, Object... params) {
    // appendLine("<span>" + string + "</span>", params);
    // return this;
    // }
    //
    // public HTMLBuilder2 pre(String string, Object... params) {
    // appendLine("<pre>" + string + "</pre>", params);
    // return this;
    // }
    //
    // public HTMLBuilder2 link(String link) {
    // appendLine("<a href='{}'>{}</a>", link, link);
    // return this;
    // }
    //
    // public HTMLBuilder2 link(String linkFormat, String variable) {
    // appendLine("<a href='{}'>{}</a>", StringUtils.format(linkFormat, variable), variable);
    // return this;
    // }
    //
    // public HTMLBuilder2 link(String text, String urlFormat, Object... variables) {
    // appendLine("<a href='{}'>{}</a>", StringUtils.format(urlFormat, variables), text);
    // return this;
    // }
    //
    // public HTMLBuilder2 endForm() {
    // appendLine("</form>");
    // return this;
    // }
    //
    // public HTMLBuilder2 formPost(String action) {
    // appendLine("<form action=\"{}\" method=\"POST\">", action);
    // return this;
    // }
    //
    // public HTMLBuilder2 formGet(String action) {
    // appendLine("<form action=\"{}\" method=\"GET\">", action);
    // return this;
    // }
    //
    // public HTMLBuilder2 form() {
    // appendLine("<form>");
    // return this;
    // }
    //
    // public HTMLBuilder2 checkbox(String group, String value, String text) {
    // appendLine("<input type=\"checkbox\" name=\"{}\" value=\"{}\">{}</input><br/>", group, value,
    // text);
    // return this;
    // }
    //
    // public HTMLBuilder2 radioButton(String group, String value, String text) {
    // appendLine("<input type=\"radio\" name=\"{}\" value=\"{}\">{}</input><br/>", group, value,
    // text);
    // return this;
    // }
    //
    // public HTMLBuilder2 submit(String text) {
    // appendLine("<input type=\"submit\" value=\"{}\">", text);
    // return this;
    // }
    //
    // public HTMLBuilder2 input(String name, String initial) {
    // appendLine("<input type=\"text\" name=\"{}\" value=\"{}\"/>", name, initial);
    // return this;
    // }
    //
    // public HTMLBuilder2 text(String string, Object... objects) {
    // append(string, objects);
    // return this;
    // }
    //
    // public HTMLBuilder2 br() {
    // appendLine("<br/>");
    // return this;
    // }
    //
    // public HTMLBuilder2 fieldset(String string) {
    // appendLine("<fieldset/>");
    // appendLine("<legend>{}</legend>", string);
    // return this;
    // }
    //
    // public HTMLBuilder2 endFieldset() {
    // appendLine("</fieldset>");
    // return this;
    // }
    //

}
