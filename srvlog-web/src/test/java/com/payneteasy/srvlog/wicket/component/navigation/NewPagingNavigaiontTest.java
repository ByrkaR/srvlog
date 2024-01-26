package com.payneteasy.srvlog.wicket.component.navigation;

import com.payneteasy.srvlog.wicket.component.navigation.service.FakeDataLoaderService;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.WicketTester;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Date: 16.01.13
 * Time: 13:10
 */
public class NewPagingNavigaiontTest  {

    WicketTester tester;
    ApplicationContextMock context;
    FakeDataLoaderService realService = new FakeDataLoaderService();

    @Before
    public void setUp() {
        context = new ApplicationContextMock();
        tester = new WicketTester(new TestDataNavigationApplication() {
            @Override
            protected void addSpring() {
                getComponentInstantiationListeners().add(new SpringComponentInjector(this, context, true));
            }
        });
    }

    @Test
    public void testNavigateToTheNextPageAnbBack () {
        FakeDataLoaderService service = EasyMock.createMock(FakeDataLoaderService.class);
        context.putBean("loaderService", service);

        EasyMock.expect(service.loadFakePageableList(0, 11)).andReturn(realService.loadFakePageableList(0, 11));
        EasyMock.expect(service.loadFakePageableList(10, 11)).andReturn(realService.loadFakePageableList(10, 11));
        EasyMock.expect(service.loadFakePageableList(0, 11)).andReturn(realService.loadFakePageableList(0, 11));
        EasyMock.replay(service);

        tester.startPage(TestDataNavigationPage.class);
        tester.assertRenderedPage(TestDataNavigationPage.class);

        tester.assertDisabled("paging-navigator:paging-first");
        tester.assertDisabled("paging-navigator:paging-previous");
        tester.assertEnabled("paging-navigator:paging-next");
        tester.assertModelValue("paging-navigator:paging-from-row", 1);
        tester.assertModelValue("paging-navigator:paging-to-row", 10);
        tester.assertInvisible("no-data");


        tester.clickLink("paging-navigator:paging-next");
        tester.assertDisabled("paging-navigator:paging-next");
        tester.assertEnabled("paging-navigator:paging-previous");
        tester.assertEnabled("paging-navigator:paging-first");
        tester.assertModelValue("paging-navigator:paging-from-row", 11);
        tester.assertModelValue("paging-navigator:paging-to-row", 15);


        tester.clickLink("paging-navigator:paging-previous");
        tester.assertDisabled("paging-navigator:paging-previous");
        tester.assertDisabled("paging-navigator:paging-first");
        tester.assertModelValue("paging-navigator:paging-from-row", 1);
        tester.assertModelValue("paging-navigator:paging-to-row", 10);

        EasyMock.verify();
    }

    @Test
    public void testNoData() {
        FakeDataLoaderService service = EasyMock.createMock(FakeDataLoaderService.class);
        context.putBean("loaderService", service);

        EasyMock.expect(service.loadFakePageableList(0, 11)).andReturn(realService.getEmptyData());
        EasyMock.replay(service);
        tester.startPage(TestDataNavigationPage.class);
        tester.assertRenderedPage(TestDataNavigationPage.class);
        tester.assertInvisible("paging-navigator");
        tester.assertVisible("no-data");

    }

    @After
    public void tearDown() {
        tester.destroy();
    }



}
