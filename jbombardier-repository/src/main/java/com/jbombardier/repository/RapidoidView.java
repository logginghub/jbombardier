package com.jbombardier.repository;

import com.jbombardier.repository.model.RepositoryModel;
import org.rapidoid.lambda.OneParamLambda;
import org.rapidoid.setup.On;

/**
 * Created by james on 20/07/2016.
 */
public class RapidoidView {

    private RepositoryController repositoryController;
    private RepositoryModel repositoryModel;

    public void configure(RepositoryController repositoryController) {
        this.repositoryController = repositoryController;
        this.repositoryModel = repositoryController.getModel();

        On.get("/size").json((String msg) -> msg.length());
        On.get("/services/configurations").plain(() -> repositoryController.getSummary());

        //On.get("/services/configuration").json((String configurationName) -> repositoryController.getConfiguration(configurationName));
        On.get("/services/configuration").plain(new OneParamLambda<String, String>() {
            @Override
            public String execute(String configurationName) throws Exception {
                return repositoryController.getConfiguration(configurationName);
            }
        });


    }

}
