package com.payneteasy.srvlog.wicket.page;

import com.payneteasy.srvlog.data.LogData;
import com.payneteasy.srvlog.data.LogFacility;
import com.payneteasy.srvlog.data.LogLevel;
import com.payneteasy.srvlog.service.ILogCollector;
import com.payneteasy.srvlog.service.IndexerServiceException;
import com.payneteasy.srvlog.util.DateRange;
import junit.framework.Assert;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.easymock.EasyMock;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Date: 14.01.13 Time: 21:18
 */
public class LogMonitorPageTest extends AbstractWicketTester {
    private ILogCollector logCollector;

    @Override
    protected void setupTest() {
        logCollector = EasyMock.createMock(ILogCollector.class);
        addBean("logCollector", logCollector);
        EasyMock.expect(logCollector.hasUnprocessedLogs()).andReturn(Boolean.TRUE).anyTimes();
    }

    @Test
    public void testSearchForPattern() throws IndexerServiceException {
        WicketTester wicketTester = getWicketTester();
        DateRange today = DateRange.today();
        List<LogData> searchLogData = getTestLogData();
        EasyMock.expect(logCollector.search(today.getFromDate(), today.getToDate(), null, null, null, null, 0, 26)).andReturn(searchLogData);
        EasyMock.expect(logCollector.search(today.getFromDate(), today.getToDate(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), "search me", 0, 26)).andReturn(searchLogData);
        EasyMock.expect(logCollector.loadHosts()).andReturn(new ArrayList<>());
        EasyMock.replay(logCollector);

        wicketTester.startPage(LogMonitorPage.class);

        FormTester formTester = wicketTester.newFormTester("form");

        formTester.setValue("pattern", "search me");

        formTester.submit("search-button");

        EasyMock.verify();
    }


    @Test
    public void testPaging() throws IndexerServiceException {
        WicketTester wicketTester = getWicketTester();
        DateRange today = DateRange.today();
        List<LogData> testLogData = getTestLogData();
        EasyMock.expect(logCollector.search(today.getFromDate(), today.getToDate(), null, null, null, null, 0, 26)).andReturn(testLogData);
        EasyMock.expect(logCollector.loadHosts()).andReturn(new ArrayList<>());
        EasyMock.expect(logCollector.search(today.getFromDate(), today.getToDate(), null, null, null, null, 25, 26)).andReturn(testLogData);
        EasyMock.expect(logCollector.search(today.getFromDate(), today.getToDate(), null, null, null, null, 0, 26)).andReturn(testLogData);
        EasyMock.expect(logCollector.search(today.getFromDate(), today.getToDate(), null, null, null, null, 25, 26)).andReturn(testLogData);
        EasyMock.expect(logCollector.search(today.getFromDate(), today.getToDate(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), null, 0, 26)).andReturn(testLogData);

        EasyMock.replay(logCollector);
        wicketTester.startPage(LogMonitorPage.class);
        wicketTester.clickLink("form:paging-navigator:paging-next");
        wicketTester.clickLink("form:paging-navigator:paging-previous");

        wicketTester.clickLink("form:paging-navigator:paging-next");

        FormTester form = wicketTester.newFormTester("form");

        form.submit("search-button");

        EasyMock.verify(logCollector);

    }

    @Test
    public void testDefaultParameter() throws IndexerServiceException {
        WicketTester wicketTester = getWicketTester();
        DateRange today = DateRange.today();

        List<LogData> searchLogData = getTestLogData();
        EasyMock.expect(logCollector.search(today.getFromDate(), today.getToDate(), null, null, null, null, 0, 26)).andReturn(searchLogData);
        EasyMock.expect(logCollector.loadHosts()).andReturn(new ArrayList<>());
        EasyMock.replay(logCollector);

        wicketTester.startPage(LogMonitorPage.class);

        EasyMock.verify(logCollector);

    }

    @Test
    public void testIndexingExceptionOccured() throws IndexerServiceException {
        WicketTester wicketTester = getWicketTester();
        DateRange today = DateRange.today();
        IndexerServiceException indexerServiceException = new IndexerServiceException("While calling indexing service", new Exception());
        EasyMock.expect(logCollector.search(today.getFromDate(), today.getToDate(), null, null, null, null, 0, 26)).andThrow(indexerServiceException);
        EasyMock.expect(logCollector.loadHosts()).andReturn(new ArrayList<>());
        EasyMock.replay(logCollector);
        wicketTester.startPage(LogMonitorPage.class);
        wicketTester.assertComponent("feedback-panel", FeedbackPanel.class);
        wicketTester.assertErrorMessages("Error while retrieving log data: While calling indexing service");
        EasyMock.verify(logCollector);

    }

    @Test
    public void testListMultipleSearch() throws IndexerServiceException {
        WicketTester wicketTester = getWicketTester();

        DateRange today = DateRange.today();
        List<LogData> searchLogData = getTestLogData();
        EasyMock.expect(logCollector.search(today.getFromDate(), today.getToDate(), null, null, null, null, 0, 26)).andReturn(searchLogData);
        EasyMock.expect(logCollector.loadHosts()).andReturn(new ArrayList<>());
        EasyMock.expect(logCollector.search(today.getFromDate(), today.getToDate(), Arrays.asList(LogFacility.kern.getValue(), LogFacility.user.getValue()), Arrays.asList(LogLevel.EMERGENCY.getValue()), new ArrayList<>(), null, 0, 26)).andReturn(searchLogData);
        EasyMock.replay(logCollector);

        wicketTester.startPage(LogMonitorPage.class);
        wicketTester.assertRenderedPage(LogMonitorPage.class);

        ListMultipleChoice<LogLevel> severityListMultipleChoice = (ListMultipleChoice<LogLevel>) wicketTester.getComponentFromLastRenderedPage("form:severity-choice");
        Assert.assertEquals(LogLevel.values().length, severityListMultipleChoice.getChoices().size());

        ListMultipleChoice<LogLevel> facilityListMultipleChoice = (ListMultipleChoice<LogLevel>) wicketTester.getComponentFromLastRenderedPage("form:facility-choice");
        Assert.assertEquals(LogFacility.values().length, facilityListMultipleChoice.getChoices().size());

        FormTester formTester = wicketTester.newFormTester("form");
        formTester.select("severity-choice", 0);

        formTester.select("facility-choice", 0);
        formTester.select("facility-choice", 1);

        formTester.submit("search-button");

        EasyMock.verify(logCollector);

    }

    private List<LogData> getTestLogData() {
        ArrayList<LogData> listData = new ArrayList<>();
        for (int i = 1; i <=30; i++) {
            LogData logData = new LogData();
            logData.setSeverity(1);
            logData.setFacility(1);
            logData.setHost("localhost");
            logData.setDate(new Date());
            logData.setId(Long.valueOf(i));
            listData.add(logData);
        }
        return listData;
    }
}
