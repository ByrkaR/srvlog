package com.payneteasy.srvlog.wicket;

import com.payneteasy.srvlog.wicket.page.DashboardPage;
import com.payneteasy.srvlog.wicket.page.LogMonitorPage;
import com.payneteasy.srvlog.wicket.page.LoginPage;
import com.payneteasy.srvlog.wicket.page.OnlineLogMonitorPage;
import com.payneteasy.srvlog.wicket.page.SnortLogMonitorPage;
import com.payneteasy.srvlog.wicket.page.TerminalPage;
import com.payneteasy.srvlog.wicket.security.SrvlogAuthorizationStrategy;
import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.core.request.mapper.CryptoMapper;
import org.apache.wicket.core.util.crypt.KeyInSessionSunJceCryptFactory;
import org.apache.wicket.csp.CSPDirective;
import org.apache.wicket.csp.CSPDirectiveSrcValue;
import org.apache.wicket.markup.head.ResourceAggregator;
import org.apache.wicket.markup.head.filter.JavaScriptFilteredIntoFooterHeaderResponse;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.UrlResourceReference;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.file.IResourceFinder;
import org.apache.wicket.util.file.Path;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 21.12.12 Time: 15:50
 */
public class SrvlogUIApplication extends WebApplication{
    @Override
    protected void init() {
        super.init();
        //TODO think what to do with wicket csp
        getCspSettings().blocking().disabled();

        getSecuritySettings().setCryptFactory(new KeyInSessionSunJceCryptFactory());
        IRequestMapper cryptoMapper = new CryptoMapper(getRootRequestMapper(), this);

        setRootRequestMapper(cryptoMapper);

        getResourceSettings().setThrowExceptionOnMissingResource(false);

        if (this.getConfigurationType() == RuntimeConfigurationType.DEVELOPMENT) {
            List<IResourceFinder> resourceFinders = new ArrayList<>();
            resourceFinders.add(new Path("../srvlog-web/src/main/java"));
            resourceFinders.addAll(getResourceSettings().getResourceFinders());
            getResourceSettings().setResourceFinders(resourceFinders);
        }

        getJavaScriptLibrarySettings().setJQueryReference(new UrlResourceReference(Url.parse("https://cdnjs.cloudflare.com/ajax/libs/jquery/3.5.0/jquery.min.js")));

        String skipSpringSecurity = getServletContext().getInitParameter("skipSpringSecurity");
        if(skipSpringSecurity!=null && !isTrue(skipSpringSecurity)){
            getSecuritySettings().setAuthorizationStrategy(new SrvlogAuthorizationStrategy());
        }

        addSpringComponentInjector();
        //PAGES
        mountPage("main", DashboardPage.class);
        mountPage("logs", LogMonitorPage.class);
        mountPage("snort-logs", SnortLogMonitorPage.class);
        mountPage("online-logs", OnlineLogMonitorPage.class);
        mountPage("terminal", TerminalPage.class);

        mountPage("login", LoginPage.class);

        getHeaderResponseDecorators().add(
                response -> new ResourceAggregator(
                        new JavaScriptFilteredIntoFooterHeaderResponse(response, "footer-container")
                )
        );
    }

//    @Override
//    protected IRequestCycleProcessor newRequestCycleProcessor() {
//
//        return new WebRequestCycleProcessor() {
//            protected IRequestCodingStrategy newRequestCodingStrategy() {
//                return new CryptedUrlWebRequestCodingStrategy(
//                        new WebRequestCodingStrategy());
//            }
//        };
//    }





    protected void addSpringComponentInjector(){
        getComponentInstantiationListeners().add(new SpringComponentInjector(this));
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return DashboardPage.class;
    }

    private boolean isTrue(String type){
        return "true".equals(type);
    }

}
