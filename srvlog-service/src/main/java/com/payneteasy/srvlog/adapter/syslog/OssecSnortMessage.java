package com.payneteasy.srvlog.adapter.syslog;

import static com.google.common.hash.Hashing.md5;
import com.nesscomputing.syslog4j.server.SyslogServerEventIF;
import com.payneteasy.srvlog.data.LogData;
import com.payneteasy.srvlog.data.OssecLogData;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import static java.util.Locale.ENGLISH;
import jregex.Matcher;
import jregex.Pattern;
import org.joda.time.DateTime;

/**
 * Object representation of snort message, processed by ossec.
 *
 * @author imenem
 */
public class OssecSnortMessage {

    /**
     * Regular expression to parse message.
     */
    private final static Pattern MESSAGE_REGEX = new Pattern(
        "({DATE}[A-Z][a-z]{2} +\\d{1,2} \\d{1,2}:\\d{2}:\\d{2})" +
        ".+? ossec: Alert Level:.+?" +
        "snort\\[\\d+\\]: " +
        "({IDENTIFIER}\\[\\d+:\\d+:\\d+\\])"
    );

    /**
     * Return true, if message generated by ossec and contains message from snort.
     *
     * @param       rawMessage      Message to ckeck.
     *
     * @return      True, if message generated by ossec and contains message from snort.
     */
    public static boolean isSnortMessageFromOssec(String rawMessage) {
        return MESSAGE_REGEX.matcher(rawMessage).find();
    }

    /**
     * Creates message generated by ossec and contains message from snort
     * from data transfer object.
     *
     * @param       ossecLogData        Data transfer object.
     *
     * @return      Object representation of snort message, processed by ossec.
     */
    public static OssecSnortMessage createOssecSnortMessage(OssecLogData ossecLogData) {
        OssecSnortMessage snortMessage = new OssecSnortMessage();

        snortMessage.setDate(ossecLogData.getDate());
        snortMessage.identifier = ossecLogData.getIdentifier();
        snortMessage.hash = ossecLogData.getHash();
        snortMessage.logDataId = ossecLogData.getLogId();

        return snortMessage;
    }

    /**
     * Parses message generated by ossec and contains message from snort.
     *
     * @param       rawMessage      Message to parse.
     *
     * @return      Object representation of snort message, processed by ossec.
     */
    public static OssecSnortMessage createOssecSnortMessage(String rawMessage) {
        OssecSnortMessage snortMessage = new OssecSnortMessage();

        Matcher matcher = MESSAGE_REGEX.matcher(rawMessage);

        if (!matcher.find()) {
            throw new RuntimeException(
                "Message does not look like message from Ossec with snort alert. " +
                "Check message with method isSnortMessageFromOssec() first."
            );
        }

        snortMessage.setDate(matcher.group("DATE"));
        snortMessage.identifier = matcher.group("IDENTIFIER");
        snortMessage.hash = md5().hashString(rawMessage, UTF_8).toString();

        return snortMessage;
    }

    /**
     * Return raw message string from syslog server event.
     *
     * @param       event       Syslog server event.
     *
     * @return      Raw message from event.
     */
    public static String getRawMessage(SyslogServerEventIF event) {
        return new String(event.getRaw()).replaceFirst("^<\\d+>", "");
    }

    /**
     * Date and time parser.
     *
     * {@link DateFormat} isn't thread safe, so we must create
     * own instance of {@link DateFormat} for each {@link SnortMessage} object.
     */
    private final DateFormat dateParser = new SimpleDateFormat("yyyy MMM d HH:mm:ss", ENGLISH);

    /**
     * General log message.
     */
    private Long logDataId;

    /**
     * Date and time of message creation.
     */
    private DateTime date;

    /**
     * Date and time, when ossec finished incoming messages collecting and generated this message.
     */
    private DateTime dateTo;

    /**
     * Date and time, when ossec possibly started incoming messages collecting.
     */
    private DateTime dateFrom;

    /**
     * Message identifier, generated by snort.
     */
    private String identifier;

    /**
     * Original ossec message hash.
     */
    private String hash;

    /**
     * Sets general log message.
     *
     * @param       logData         General log message.
     */
    public void setLogData(LogData logData) {
        this.logDataId = logData.getId();
    }

    /**
     * Returns message identifier, generated by snort.
     *
     * @return      Message identifier.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Returns original ossec message hash.
     *
     * @return      Message hash.
     */
    public String getHash() {
        return hash;
    }

    /**
     * Returns date and time, when ossec finished incoming messages collecting and generated this message.
     *
     * @return      Date and time, when ossec finished incoming messages collecting and generated this message.
     */
    public Date getDateTo() {
        return dateTo.toDate();
    }

    /**
     * Returns date and time, when ossec possibly started incoming messages collecting.
     *
     * @return      Date and time, when ossec possibly started incoming messages collecting.
     */
    public Date getDateFrom() {
        return dateFrom.toDate();
    }

    /**
     * Converts object to data transfer object.
     *
     * @return      Data transfer object.
     */
    public OssecLogData toOssecLogData() {
        OssecLogData osseclogData = new OssecLogData();

        if (logDataId == null) {
            throw new RuntimeException("You must set saved LogData first");
        }

        osseclogData.setLogId(logDataId);
        osseclogData.setIdentifier(getIdentifier());
        osseclogData.setHash(getHash());
        osseclogData.setDate(date.toDate());

        return osseclogData;
    }

    /**
     * Converts date from string to object and assigns it to object field.
     *
     * @param       date        Date and time, when message generated in string form.
     */
    private void setDate(String dateString) {
        // Date has no year, so we must set in here.
        String fullDate = new DateTime().getYear() + " " + dateString.replaceAll(" +", " ");

        try {
            setDate(dateParser.parse(fullDate));
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Converts date from string to object and assigns it to object field.
     *
     * @param       date        Date and time, when message generated in string form.
     */
    private void setDate(Date newDate) {
        date = new DateTime(newDate);

        dateTo = date.plusMinutes(1);
        dateFrom = date.minusMinutes(5);
    }

}
