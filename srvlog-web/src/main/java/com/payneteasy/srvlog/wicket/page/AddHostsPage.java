package com.payneteasy.srvlog.wicket.page;

import com.payneteasy.srvlog.data.HostData;
import com.payneteasy.srvlog.service.ILogCollector;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Date: 29.01.13 Time: 20:13
 */
public class AddHostsPage extends BasePage{

    @SpringBean
    private ILogCollector logCollector;

    public AddHostsPage(PageParameters pageParameters) {
        super(pageParameters, AddHostsPage.class);

        FeedbackPanel feedbackPanel = new FeedbackPanel("feedbackPanel");
        add(feedbackPanel);

        FormModel formModel = new FormModel();

        if(!pageParameters.isEmpty() && pageParameters.getNamedKeys().contains(BasePage.HAS_UNPROCESSED_HOSTS_PARAMETER)){
            LinkedList<String> unprocessedHosts = new LinkedList<>(logCollector.getUnprocessedHostsName());
            if(unprocessedHosts.isEmpty()) return;
            StringBuilder resultString = new StringBuilder();
            for (String unprocessedHost : unprocessedHosts) {
                resultString.append(unprocessedHost);
                if(!unprocessedHosts.getLast().equals(unprocessedHost)){
                    resultString.append(";");
                }
            }
            formModel.setHosts(resultString.toString());
        }


        final Form<FormModel> form = new Form<>("form", new CompoundPropertyModel<>(formModel));
        add(form);

        TextArea<String> textArea = new TextArea<>("hosts");
        textArea.setRequired(true);
        form.add(textArea);

        form.add(new Button("button"){
            @Override
            public void onSubmit() {
                FormModel formModel = form.getModelObject();
                try{
                    if (parseHosts(formModel.getHosts())){
                        info(new ResourceModel("addHost.info").getObject());
                        setResponsePage(LogMonitorPage.class);
                    }else{
                        error(new ResourceModel("addHost.error").getObject());
                    }
                }catch (RuntimeException e){
                     error(new ResourceModel("addHost.duplicateError").getObject());
                }

            }
        });
    }

    public class FormModel implements Serializable{
        private String hosts;

        public String getHosts() {
            return hosts;
        }

        public void setHosts(String hosts) {
            this.hosts = hosts;
        }
    }

    private boolean parseHosts(String originValue){
        String value = originValue.trim();
        String[] hostsDataArray = value.split(";");
        if(hostsDataArray.length <= 0){
            return false;
        }
        List<HostData> hostDataList = new ArrayList<>();
        for (String hostDataString : hostsDataArray) {
            String[] host = hostDataString.split(",");
            if(host.length > 2){
               return false;
            }

            HostData hostData;
            if(host.length == 1){
                hostData = new HostData();
                hostData.setHostname(host[0]);
                hostData.setIpAddress("");
            }else {
                hostData = new HostData();
                hostData.setHostname(host[0]);
                hostData.setIpAddress(host[1]);
            }
            hostDataList.add(hostData);
        }
        logCollector.saveHosts(hostDataList);
        return true;
    }
}
