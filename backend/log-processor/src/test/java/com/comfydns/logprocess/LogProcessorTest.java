package com.comfydns.logprocess;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class LogProcessorTest {
    @Test
    public void test() throws IOException {
        StringReader r = new StringReader(
                "/usr/lib/jvm/java-11-openjdk/bin/java -Dorg.slf4j.simpleLogger.log.com.comfydns.resolver=DEBUG -javaagent:/usr/share/idea/lib/idea_rt.jar=46137:/usr/share/idea/bin -Dfile.encoding=UTF-8 -classpath /home/josh/projects/comfydns/backend/recursor/target/classes:/home/josh/.m2/repository/io/netty/netty-all/4.1.50.Final/netty-all-4.1.50.Final.jar:/home/josh/.m2/repository/org/yaml/snakeyaml/1.25/snakeyaml-1.25.jar:/home/josh/.m2/repository/net/sf/jopt-simple/jopt-simple/4.9/jopt-simple-4.9.jar:/home/josh/.m2/repository/org/slf4j/slf4j-api/1.7.30/slf4j-api-1.7.30.jar:/home/josh/.m2/repository/org/slf4j/slf4j-simple/1.7.30/slf4j-simple-1.7.30.jar:/home/josh/.m2/repository/org/postgresql/postgresql/42.2.12/postgresql-42.2.12.jar:/home/josh/.m2/repository/com/google/code/gson/gson/2.8.6/gson-2.8.6.jar:/home/josh/.m2/repository/io/prometheus/simpleclient/0.10.0/simpleclient-0.10.0.jar:/home/josh/.m2/repository/io/prometheus/simpleclient_hotspot/0.10.0/simpleclient_hotspot-0.10.0.jar:/home/josh/.m2/repository/io/prometheus/simpleclient_httpserver/0.10.0/simpleclient_httpserver-0.10.0.jar:/home/josh/.m2/repository/io/prometheus/simpleclient_common/0.10.0/simpleclient_common-0.10.0.jar:/home/josh/.m2/repository/com/squareup/okhttp3/okhttp/4.9.0/okhttp-4.9.0.jar:/home/josh/.m2/repository/com/squareup/okio/okio/2.8.0/okio-2.8.0.jar:/home/josh/.m2/repository/org/jetbrains/kotlin/kotlin-stdlib-common/1.4.0/kotlin-stdlib-common-1.4.0.jar:/home/josh/.m2/repository/org/jetbrains/kotlin/kotlin-stdlib/1.4.10/kotlin-stdlib-1.4.10.jar:/home/josh/.m2/repository/org/jetbrains/annotations/13.0/annotations-13.0.jar:/home/josh/.m2/repository/org/apache/httpcomponents/fluent-hc/4.5.12/fluent-hc-4.5.12.jar:/home/josh/.m2/repository/org/apache/httpcomponents/httpclient/4.5.12/httpclient-4.5.12.jar:/home/josh/.m2/repository/org/apache/httpcomponents/httpcore/4.4.13/httpcore-4.4.13.jar:/home/josh/.m2/repository/commons-codec/commons-codec/1.11/commons-codec-1.11.jar:/home/josh/.m2/repository/commons-logging/commons-logging/1.2/commons-logging-1.2.jar com.comfydns.resolver.ComfyNameDaemon\n" +
                        "2022-04-01T21:28:41.648-0500 INFO ComfyNameDaemon - [Startup] Waiting for resolver to be ready...\n" +
                        "2022-04-01T21:28:41.672-0500 DEBUG ComfyResolverThread - Not starting DOH HTTP server.\n" +
                        "2022-04-01T21:28:41.672-0500 INFO ComfyResolverThread - Resolver ready.\n" +
                        "2022-04-01T21:28:42.673-0500 INFO ComfyNameDaemon - [Startup] Startup complete!\n" +
                        "2022-04-01T21:28:51.053-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_IN\",\"eventTime\":\"2022-04-02T02:28:51.045476Z\",\"id\":\"a30c4949-6e01-4f39-a0f2-1fa1c369579b\",\"class\":\"UDPRequest\",\"numQuestions\":1,\"questions\":[{\"qname\":\"google.com\",\"qtype\":1,\"qclass\":1}]}\n" +
                        "2022-04-01T21:28:51.054-0500 INFO RecursiveResolver - [Q] [127.0.0.1]: QNAME: google.com, QTYPE: A, QCLASS: IN | a30c4949-6e01-4f39-a0f2-1fa1c369579b\n" +
                        "2022-04-01T21:28:51.058-0500 DEBUG RecursiveResolverTask - [a30c4949-6e01-4f39-a0f2-1fa1c369579b]: STATE INITIAL_CHECKING -> SNAME_CHECKING_STATE\n" +
                        "2022-04-01T21:28:51.058-0500 DEBUG RecursiveResolverTask - [a30c4949-6e01-4f39-a0f2-1fa1c369579b]: STATE SNAME_CHECKING_STATE -> TRY_TO_ANSWER_WITH_LOCAL_INFORMATION\n" +
                        "2022-04-01T21:28:51.067-0500 DEBUG RecursiveResolverTask - [a30c4949-6e01-4f39-a0f2-1fa1c369579b]: STATE TRY_TO_ANSWER_WITH_LOCAL_INFORMATION -> FIND_BEST_SERVER_TO_ASK\n" +
                        "2022-04-01T21:28:51.075-0500 DEBUG RecursiveResolverTask - [a30c4949-6e01-4f39-a0f2-1fa1c369579b]: STATE FIND_BEST_SERVER_TO_ASK -> SEND_SERVER_QUERY\n" +
                        "2022-04-01T21:28:51.077-0500 DEBUG SendServerQuery - [a30c4949-6e01-4f39-a0f2-1fa1c369579b]: QUERY: ns4.google.com (/216.239.38.10)\n" +
                        "2022-04-01T21:28:51.122-0500 DEBUG RecursiveResolverTask - [a30c4949-6e01-4f39-a0f2-1fa1c369579b]: STATE <callback> -> HANDLE_RESPONSE_TO_ZONE_QUERY\n" +
                        "2022-04-01T21:28:51.124-0500 DEBUG HandleResponseToZoneQuery - [a30c4949-6e01-4f39-a0f2-1fa1c369579b]: Message received: >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n" +
                        "ID: 59393, QR: response, OPCODE: QUERY, AA: true, TC: false, RD: false, RA: false, RCODE: NO_ERROR\n" +
                        "QDCOUNT: 1, ANCOUNT: 1, NSCOUNT: 0, ARCOUNT: 0\n" +
                        "===========================================================\n" +
                        "QNAME: google.com, QTYPE: A, QCLASS: IN\n" +
                        "===========================================================\n" +
                        "----------------------------------------------------------\n" +
                        "NAME: google.com, TYPE: A, CLASS: IN, TTL: 300, RDATA:\n" +
                        " address: 142.250.190.142\n" +
                        "===========================================================\n" +
                        "===========================================================\n" +
                        "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n" +
                        "\n" +
                        "2022-04-01T21:28:51.126-0500 DEBUG HandleResponseToZoneQuery - Checking for SOA -> nameerror\n" +
                        "2022-04-01T21:28:51.128-0500 DEBUG HandleResponseToZoneQuery - [a30c4949-6e01-4f39-a0f2-1fa1c369579b]: Processing RRs in response.\n" +
                        "2022-04-01T21:28:51.144-0500 DEBUG DBDNSCache - Inserted 1 rows\n" +
                        "2022-04-01T21:28:51.144-0500 DEBUG RecursiveResolverTask - [a30c4949-6e01-4f39-a0f2-1fa1c369579b]: STATE HANDLE_RESPONSE_TO_ZONE_QUERY -> TRY_TO_ANSWER_WITH_LOCAL_INFORMATION\n" +
                        "2022-04-01T21:28:51.150-0500 DEBUG RecursiveResolverTask - [a30c4949-6e01-4f39-a0f2-1fa1c369579b]: STATE TRY_TO_ANSWER_WITH_LOCAL_INFORMATION -> DOUBLE_CHECK_SEND_STATE\n" +
                        "2022-04-01T21:28:51.150-0500 DEBUG DoubleCheckSendState - Skipping double-check - no upstream configured.\n" +
                        "2022-04-01T21:28:51.151-0500 DEBUG RecursiveResolverTask - [a30c4949-6e01-4f39-a0f2-1fa1c369579b]: STATE DOUBLE_CHECK_SEND_STATE -> SEND_RESPONSE_STATE\n" +
                        "2022-04-01T21:28:51.153-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_OUT\",\"eventTime\":\"2022-04-02T02:28:51.152848Z\",\"id\":\"a30c4949-6e01-4f39-a0f2-1fa1c369579b\",\"rCode\":0}\n" +
                        "2022-04-01T21:28:52.651-0500 DEBUG ScheduledRefreshRunnable - Starting block list refresh check.\n" +
                        "2022-04-01T21:28:52.654-0500 DEBUG TaskDispatcher - No tasks to start\n" +
                        "2022-04-01T21:28:52.662-0500 DEBUG ScheduledRefreshRunnable - No block lists need refresh.\n" +
                        "2022-04-01T21:28:53.662-0500 DEBUG TaskDispatcher - No tasks to start\n" +
                        "2022-04-01T21:28:54.665-0500 DEBUG TaskDispatcher - No tasks to start\n" +
                        "2022-04-01T21:28:55.668-0500 DEBUG TaskDispatcher - No tasks to start\n" +
                        "2022-04-01T21:28:56.204-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_IN\",\"eventTime\":\"2022-04-02T02:28:56.204743Z\",\"id\":\"24db0253-3771-4e2e-adba-2dc664811652\",\"class\":\"UDPRequest\",\"numQuestions\":1,\"questions\":[{\"qname\":\"google.com\",\"qtype\":1,\"qclass\":1}]}\n" +
                        "2022-04-01T21:28:56.205-0500 INFO RecursiveResolver - [Q] [127.0.0.1]: QNAME: google.com, QTYPE: A, QCLASS: IN | 24db0253-3771-4e2e-adba-2dc664811652\n" +
                        "2022-04-01T21:28:56.205-0500 DEBUG RecursiveResolverTask - [24db0253-3771-4e2e-adba-2dc664811652]: STATE INITIAL_CHECKING -> SNAME_CHECKING_STATE\n" +
                        "2022-04-01T21:28:56.205-0500 DEBUG RecursiveResolverTask - [24db0253-3771-4e2e-adba-2dc664811652]: STATE SNAME_CHECKING_STATE -> TRY_TO_ANSWER_WITH_LOCAL_INFORMATION\n" +
                        "2022-04-01T21:28:56.210-0500 DEBUG RecursiveResolverTask - [24db0253-3771-4e2e-adba-2dc664811652]: STATE TRY_TO_ANSWER_WITH_LOCAL_INFORMATION -> DOUBLE_CHECK_SEND_STATE\n" +
                        "2022-04-01T21:28:56.210-0500 DEBUG DoubleCheckSendState - Skipping double-check - no upstream configured.\n" +
                        "2022-04-01T21:28:56.210-0500 DEBUG RecursiveResolverTask - [24db0253-3771-4e2e-adba-2dc664811652]: STATE DOUBLE_CHECK_SEND_STATE -> SEND_RESPONSE_STATE\n" +
                        "2022-04-01T21:28:56.211-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_OUT\",\"eventTime\":\"2022-04-02T02:28:56.211012Z\",\"id\":\"24db0253-3771-4e2e-adba-2dc664811652\",\"rCode\":0}\n" +
                        "2022-04-01T21:28:56.670-0500 DEBUG TaskDispatcher - No tasks to start\n" +
                        "2022-04-01T21:28:56.984-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_IN\",\"eventTime\":\"2022-04-02T02:28:56.984027Z\",\"id\":\"c9cb41b0-ef5f-4694-b06b-04c5a6020cfa\",\"class\":\"UDPRequest\",\"numQuestions\":1,\"questions\":[{\"qname\":\"google.com\",\"qtype\":1,\"qclass\":1}]}\n" +
                        "2022-04-01T21:28:56.984-0500 INFO RecursiveResolver - [Q] [127.0.0.1]: QNAME: google.com, QTYPE: A, QCLASS: IN | c9cb41b0-ef5f-4694-b06b-04c5a6020cfa\n" +
                        "2022-04-01T21:28:56.985-0500 DEBUG RecursiveResolverTask - [c9cb41b0-ef5f-4694-b06b-04c5a6020cfa]: STATE INITIAL_CHECKING -> SNAME_CHECKING_STATE\n" +
                        "2022-04-01T21:28:56.985-0500 DEBUG RecursiveResolverTask - [c9cb41b0-ef5f-4694-b06b-04c5a6020cfa]: STATE SNAME_CHECKING_STATE -> TRY_TO_ANSWER_WITH_LOCAL_INFORMATION\n" +
                        "2022-04-01T21:28:56.990-0500 DEBUG RecursiveResolverTask - [c9cb41b0-ef5f-4694-b06b-04c5a6020cfa]: STATE TRY_TO_ANSWER_WITH_LOCAL_INFORMATION -> DOUBLE_CHECK_SEND_STATE\n" +
                        "2022-04-01T21:28:56.990-0500 DEBUG DoubleCheckSendState - Skipping double-check - no upstream configured.\n" +
                        "2022-04-01T21:28:56.991-0500 DEBUG RecursiveResolverTask - [c9cb41b0-ef5f-4694-b06b-04c5a6020cfa]: STATE DOUBLE_CHECK_SEND_STATE -> SEND_RESPONSE_STATE\n" +
                        "2022-04-01T21:28:56.992-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_OUT\",\"eventTime\":\"2022-04-02T02:28:56.991828Z\",\"id\":\"c9cb41b0-ef5f-4694-b06b-04c5a6020cfa\",\"rCode\":0}\n" +
                        "2022-04-01T21:28:57.672-0500 DEBUG TaskDispatcher - No tasks to start\n" +
                        "2022-04-01T21:28:57.699-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_IN\",\"eventTime\":\"2022-04-02T02:28:57.699331Z\",\"id\":\"0a4fda12-11f9-499d-91c5-2c78d8e24d46\",\"class\":\"UDPRequest\",\"numQuestions\":1,\"questions\":[{\"qname\":\"google.com\",\"qtype\":1,\"qclass\":1}]}\n" +
                        "2022-04-01T21:28:57.699-0500 INFO RecursiveResolver - [Q] [127.0.0.1]: QNAME: google.com, QTYPE: A, QCLASS: IN | 0a4fda12-11f9-499d-91c5-2c78d8e24d46\n" +
                        "2022-04-01T21:28:57.699-0500 DEBUG RecursiveResolverTask - [0a4fda12-11f9-499d-91c5-2c78d8e24d46]: STATE INITIAL_CHECKING -> SNAME_CHECKING_STATE\n" +
                        "2022-04-01T21:28:57.699-0500 DEBUG RecursiveResolverTask - [0a4fda12-11f9-499d-91c5-2c78d8e24d46]: STATE SNAME_CHECKING_STATE -> TRY_TO_ANSWER_WITH_LOCAL_INFORMATION\n" +
                        "2022-04-01T21:28:57.701-0500 DEBUG RecursiveResolverTask - [0a4fda12-11f9-499d-91c5-2c78d8e24d46]: STATE TRY_TO_ANSWER_WITH_LOCAL_INFORMATION -> DOUBLE_CHECK_SEND_STATE\n" +
                        "2022-04-01T21:28:57.701-0500 DEBUG DoubleCheckSendState - Skipping double-check - no upstream configured.\n" +
                        "2022-04-01T21:28:57.701-0500 DEBUG RecursiveResolverTask - [0a4fda12-11f9-499d-91c5-2c78d8e24d46]: STATE DOUBLE_CHECK_SEND_STATE -> SEND_RESPONSE_STATE\n" +
                        "2022-04-01T21:28:57.702-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_OUT\",\"eventTime\":\"2022-04-02T02:28:57.702153Z\",\"id\":\"0a4fda12-11f9-499d-91c5-2c78d8e24d46\",\"rCode\":0}\n" +
                        "2022-04-01T21:28:58.010-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_IN\",\"eventTime\":\"2022-04-02T02:28:58.009997Z\",\"id\":\"dabf0d03-798d-4fe0-8670-c3a445859f70\",\"class\":\"UDPRequest\",\"numQuestions\":1,\"questions\":[{\"qname\":\"google.com\",\"qtype\":1,\"qclass\":1}]}\n" +
                        "2022-04-01T21:28:58.010-0500 INFO RecursiveResolver - [Q] [127.0.0.1]: QNAME: google.com, QTYPE: A, QCLASS: IN | dabf0d03-798d-4fe0-8670-c3a445859f70\n" +
                        "2022-04-01T21:28:58.011-0500 DEBUG RecursiveResolverTask - [dabf0d03-798d-4fe0-8670-c3a445859f70]: STATE INITIAL_CHECKING -> SNAME_CHECKING_STATE\n" +
                        "2022-04-01T21:28:58.011-0500 DEBUG RecursiveResolverTask - [dabf0d03-798d-4fe0-8670-c3a445859f70]: STATE SNAME_CHECKING_STATE -> TRY_TO_ANSWER_WITH_LOCAL_INFORMATION\n" +
                        "2022-04-01T21:28:58.015-0500 DEBUG RecursiveResolverTask - [dabf0d03-798d-4fe0-8670-c3a445859f70]: STATE TRY_TO_ANSWER_WITH_LOCAL_INFORMATION -> DOUBLE_CHECK_SEND_STATE\n" +
                        "2022-04-01T21:28:58.015-0500 DEBUG DoubleCheckSendState - Skipping double-check - no upstream configured.\n" +
                        "2022-04-01T21:28:58.015-0500 DEBUG RecursiveResolverTask - [dabf0d03-798d-4fe0-8670-c3a445859f70]: STATE DOUBLE_CHECK_SEND_STATE -> SEND_RESPONSE_STATE\n" +
                        "2022-04-01T21:28:58.015-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_OUT\",\"eventTime\":\"2022-04-02T02:28:58.015772Z\",\"id\":\"dabf0d03-798d-4fe0-8670-c3a445859f70\",\"rCode\":0}\n" +
                        "2022-04-01T21:28:58.282-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_IN\",\"eventTime\":\"2022-04-02T02:28:58.282608Z\",\"id\":\"77e2586d-37d2-45b8-bde1-78c943c54f49\",\"class\":\"UDPRequest\",\"numQuestions\":1,\"questions\":[{\"qname\":\"google.com\",\"qtype\":1,\"qclass\":1}]}\n" +
                        "2022-04-01T21:28:58.283-0500 INFO RecursiveResolver - [Q] [127.0.0.1]: QNAME: google.com, QTYPE: A, QCLASS: IN | 77e2586d-37d2-45b8-bde1-78c943c54f49\n" +
                        "2022-04-01T21:28:58.283-0500 DEBUG RecursiveResolverTask - [77e2586d-37d2-45b8-bde1-78c943c54f49]: STATE INITIAL_CHECKING -> SNAME_CHECKING_STATE\n" +
                        "2022-04-01T21:28:58.283-0500 DEBUG RecursiveResolverTask - [77e2586d-37d2-45b8-bde1-78c943c54f49]: STATE SNAME_CHECKING_STATE -> TRY_TO_ANSWER_WITH_LOCAL_INFORMATION\n" +
                        "2022-04-01T21:28:58.286-0500 DEBUG RecursiveResolverTask - [77e2586d-37d2-45b8-bde1-78c943c54f49]: STATE TRY_TO_ANSWER_WITH_LOCAL_INFORMATION -> DOUBLE_CHECK_SEND_STATE\n" +
                        "2022-04-01T21:28:58.286-0500 DEBUG DoubleCheckSendState - Skipping double-check - no upstream configured.\n" +
                        "2022-04-01T21:28:58.286-0500 DEBUG RecursiveResolverTask - [77e2586d-37d2-45b8-bde1-78c943c54f49]: STATE DOUBLE_CHECK_SEND_STATE -> SEND_RESPONSE_STATE\n" +
                        "2022-04-01T21:28:58.286-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_OUT\",\"eventTime\":\"2022-04-02T02:28:58.286597Z\",\"id\":\"77e2586d-37d2-45b8-bde1-78c943c54f49\",\"rCode\":0}\n" +
                        "2022-04-01T21:28:58.594-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_IN\",\"eventTime\":\"2022-04-02T02:28:58.593884Z\",\"id\":\"989a6cfb-702e-46ee-9fc0-55bd305ab27a\",\"class\":\"UDPRequest\",\"numQuestions\":1,\"questions\":[{\"qname\":\"google.com\",\"qtype\":1,\"qclass\":1}]}\n" +
                        "2022-04-01T21:28:58.594-0500 INFO RecursiveResolver - [Q] [127.0.0.1]: QNAME: google.com, QTYPE: A, QCLASS: IN | 989a6cfb-702e-46ee-9fc0-55bd305ab27a\n" +
                        "2022-04-01T21:28:58.594-0500 DEBUG RecursiveResolverTask - [989a6cfb-702e-46ee-9fc0-55bd305ab27a]: STATE INITIAL_CHECKING -> SNAME_CHECKING_STATE\n" +
                        "2022-04-01T21:28:58.594-0500 DEBUG RecursiveResolverTask - [989a6cfb-702e-46ee-9fc0-55bd305ab27a]: STATE SNAME_CHECKING_STATE -> TRY_TO_ANSWER_WITH_LOCAL_INFORMATION\n" +
                        "2022-04-01T21:28:58.597-0500 DEBUG RecursiveResolverTask - [989a6cfb-702e-46ee-9fc0-55bd305ab27a]: STATE TRY_TO_ANSWER_WITH_LOCAL_INFORMATION -> DOUBLE_CHECK_SEND_STATE\n" +
                        "2022-04-01T21:28:58.597-0500 DEBUG DoubleCheckSendState - Skipping double-check - no upstream configured.\n" +
                        "2022-04-01T21:28:58.597-0500 DEBUG RecursiveResolverTask - [989a6cfb-702e-46ee-9fc0-55bd305ab27a]: STATE DOUBLE_CHECK_SEND_STATE -> SEND_RESPONSE_STATE\n" +
                        "2022-04-01T21:28:58.598-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_OUT\",\"eventTime\":\"2022-04-02T02:28:58.597924Z\",\"id\":\"989a6cfb-702e-46ee-9fc0-55bd305ab27a\",\"rCode\":0}\n" +
                        "2022-04-01T21:28:58.674-0500 DEBUG TaskDispatcher - No tasks to start\n" +
                        "2022-04-01T21:28:58.896-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_IN\",\"eventTime\":\"2022-04-02T02:28:58.896027Z\",\"id\":\"d9eaaa58-2b10-4d07-996e-a65b1b78119b\",\"class\":\"UDPRequest\",\"numQuestions\":1,\"questions\":[{\"qname\":\"google.com\",\"qtype\":1,\"qclass\":1}]}\n" +
                        "2022-04-01T21:28:58.896-0500 INFO RecursiveResolver - [Q] [127.0.0.1]: QNAME: google.com, QTYPE: A, QCLASS: IN | d9eaaa58-2b10-4d07-996e-a65b1b78119b\n" +
                        "2022-04-01T21:28:58.896-0500 DEBUG RecursiveResolverTask - [d9eaaa58-2b10-4d07-996e-a65b1b78119b]: STATE INITIAL_CHECKING -> SNAME_CHECKING_STATE\n" +
                        "2022-04-01T21:28:58.896-0500 DEBUG RecursiveResolverTask - [d9eaaa58-2b10-4d07-996e-a65b1b78119b]: STATE SNAME_CHECKING_STATE -> TRY_TO_ANSWER_WITH_LOCAL_INFORMATION\n" +
                        "2022-04-01T21:28:58.899-0500 DEBUG RecursiveResolverTask - [d9eaaa58-2b10-4d07-996e-a65b1b78119b]: STATE TRY_TO_ANSWER_WITH_LOCAL_INFORMATION -> DOUBLE_CHECK_SEND_STATE\n" +
                        "2022-04-01T21:28:58.899-0500 DEBUG DoubleCheckSendState - Skipping double-check - no upstream configured.\n" +
                        "2022-04-01T21:28:58.900-0500 DEBUG RecursiveResolverTask - [d9eaaa58-2b10-4d07-996e-a65b1b78119b]: STATE DOUBLE_CHECK_SEND_STATE -> SEND_RESPONSE_STATE\n" +
                        "2022-04-01T21:28:58.900-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_OUT\",\"eventTime\":\"2022-04-02T02:28:58.900513Z\",\"id\":\"d9eaaa58-2b10-4d07-996e-a65b1b78119b\",\"rCode\":0}\n" +
                        "2022-04-01T21:28:59.183-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_IN\",\"eventTime\":\"2022-04-02T02:28:59.183509Z\",\"id\":\"35dab412-466a-4aa7-b062-484c84858b64\",\"class\":\"UDPRequest\",\"numQuestions\":1,\"questions\":[{\"qname\":\"google.com\",\"qtype\":1,\"qclass\":1}]}\n" +
                        "2022-04-01T21:28:59.184-0500 INFO RecursiveResolver - [Q] [127.0.0.1]: QNAME: google.com, QTYPE: A, QCLASS: IN | 35dab412-466a-4aa7-b062-484c84858b64\n" +
                        "2022-04-01T21:28:59.184-0500 DEBUG RecursiveResolverTask - [35dab412-466a-4aa7-b062-484c84858b64]: STATE INITIAL_CHECKING -> SNAME_CHECKING_STATE\n" +
                        "2022-04-01T21:28:59.184-0500 DEBUG RecursiveResolverTask - [35dab412-466a-4aa7-b062-484c84858b64]: STATE SNAME_CHECKING_STATE -> TRY_TO_ANSWER_WITH_LOCAL_INFORMATION\n" +
                        "2022-04-01T21:28:59.188-0500 DEBUG RecursiveResolverTask - [35dab412-466a-4aa7-b062-484c84858b64]: STATE TRY_TO_ANSWER_WITH_LOCAL_INFORMATION -> DOUBLE_CHECK_SEND_STATE\n" +
                        "2022-04-01T21:28:59.188-0500 DEBUG DoubleCheckSendState - Skipping double-check - no upstream configured.\n" +
                        "2022-04-01T21:28:59.188-0500 DEBUG RecursiveResolverTask - [35dab412-466a-4aa7-b062-484c84858b64]: STATE DOUBLE_CHECK_SEND_STATE -> SEND_RESPONSE_STATE\n" +
                        "2022-04-01T21:28:59.188-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_OUT\",\"eventTime\":\"2022-04-02T02:28:59.188813Z\",\"id\":\"35dab412-466a-4aa7-b062-484c84858b64\",\"rCode\":0}\n" +
                        "2022-04-01T21:28:59.466-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_IN\",\"eventTime\":\"2022-04-02T02:28:59.465808Z\",\"id\":\"d5eeb413-0c3d-41e2-bae2-a12674296bd6\",\"class\":\"UDPRequest\",\"numQuestions\":1,\"questions\":[{\"qname\":\"google.com\",\"qtype\":1,\"qclass\":1}]}\n" +
                        "2022-04-01T21:28:59.466-0500 INFO RecursiveResolver - [Q] [127.0.0.1]: QNAME: google.com, QTYPE: A, QCLASS: IN | d5eeb413-0c3d-41e2-bae2-a12674296bd6\n" +
                        "2022-04-01T21:28:59.466-0500 DEBUG RecursiveResolverTask - [d5eeb413-0c3d-41e2-bae2-a12674296bd6]: STATE INITIAL_CHECKING -> SNAME_CHECKING_STATE\n" +
                        "2022-04-01T21:28:59.467-0500 DEBUG RecursiveResolverTask - [d5eeb413-0c3d-41e2-bae2-a12674296bd6]: STATE SNAME_CHECKING_STATE -> TRY_TO_ANSWER_WITH_LOCAL_INFORMATION\n" +
                        "2022-04-01T21:28:59.471-0500 DEBUG RecursiveResolverTask - [d5eeb413-0c3d-41e2-bae2-a12674296bd6]: STATE TRY_TO_ANSWER_WITH_LOCAL_INFORMATION -> DOUBLE_CHECK_SEND_STATE\n" +
                        "2022-04-01T21:28:59.471-0500 DEBUG DoubleCheckSendState - Skipping double-check - no upstream configured.\n" +
                        "2022-04-01T21:28:59.471-0500 DEBUG RecursiveResolverTask - [d5eeb413-0c3d-41e2-bae2-a12674296bd6]: STATE DOUBLE_CHECK_SEND_STATE -> SEND_RESPONSE_STATE\n" +
                        "2022-04-01T21:28:59.472-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_OUT\",\"eventTime\":\"2022-04-02T02:28:59.472522Z\",\"id\":\"d5eeb413-0c3d-41e2-bae2-a12674296bd6\",\"rCode\":0}\n" +
                        "2022-04-01T21:28:59.676-0500 DEBUG TaskDispatcher - No tasks to start\n" +
                        "2022-04-01T21:28:59.799-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_IN\",\"eventTime\":\"2022-04-02T02:28:59.799112Z\",\"id\":\"216c8fdf-988e-4b41-87f7-fa60908c1164\",\"class\":\"UDPRequest\",\"numQuestions\":1,\"questions\":[{\"qname\":\"google.com\",\"qtype\":1,\"qclass\":1}]}\n" +
                        "2022-04-01T21:28:59.799-0500 INFO RecursiveResolver - [Q] [127.0.0.1]: QNAME: google.com, QTYPE: A, QCLASS: IN | 216c8fdf-988e-4b41-87f7-fa60908c1164\n" +
                        "2022-04-01T21:28:59.799-0500 DEBUG RecursiveResolverTask - [216c8fdf-988e-4b41-87f7-fa60908c1164]: STATE INITIAL_CHECKING -> SNAME_CHECKING_STATE\n" +
                        "2022-04-01T21:28:59.799-0500 DEBUG RecursiveResolverTask - [216c8fdf-988e-4b41-87f7-fa60908c1164]: STATE SNAME_CHECKING_STATE -> TRY_TO_ANSWER_WITH_LOCAL_INFORMATION\n" +
                        "2022-04-01T21:28:59.803-0500 DEBUG RecursiveResolverTask - [216c8fdf-988e-4b41-87f7-fa60908c1164]: STATE TRY_TO_ANSWER_WITH_LOCAL_INFORMATION -> DOUBLE_CHECK_SEND_STATE\n" +
                        "2022-04-01T21:28:59.803-0500 DEBUG DoubleCheckSendState - Skipping double-check - no upstream configured.\n" +
                        "2022-04-01T21:28:59.803-0500 DEBUG RecursiveResolverTask - [216c8fdf-988e-4b41-87f7-fa60908c1164]: STATE DOUBLE_CHECK_SEND_STATE -> SEND_RESPONSE_STATE\n" +
                        "2022-04-01T21:28:59.804-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_OUT\",\"eventTime\":\"2022-04-02T02:28:59.804297Z\",\"id\":\"216c8fdf-988e-4b41-87f7-fa60908c1164\",\"rCode\":0}\n" +
                        "2022-04-01T21:29:00.075-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_IN\",\"eventTime\":\"2022-04-02T02:29:00.075076Z\",\"id\":\"f7ec0429-e8bd-466e-b4e7-9047e0ad607f\",\"class\":\"UDPRequest\",\"numQuestions\":1,\"questions\":[{\"qname\":\"google.com\",\"qtype\":1,\"qclass\":1}]}\n" +
                        "2022-04-01T21:29:00.075-0500 INFO RecursiveResolver - [Q] [127.0.0.1]: QNAME: google.com, QTYPE: A, QCLASS: IN | f7ec0429-e8bd-466e-b4e7-9047e0ad607f\n" +
                        "2022-04-01T21:29:00.076-0500 DEBUG RecursiveResolverTask - [f7ec0429-e8bd-466e-b4e7-9047e0ad607f]: STATE INITIAL_CHECKING -> SNAME_CHECKING_STATE\n" +
                        "2022-04-01T21:29:00.076-0500 DEBUG RecursiveResolverTask - [f7ec0429-e8bd-466e-b4e7-9047e0ad607f]: STATE SNAME_CHECKING_STATE -> TRY_TO_ANSWER_WITH_LOCAL_INFORMATION\n" +
                        "2022-04-01T21:29:00.079-0500 DEBUG RecursiveResolverTask - [f7ec0429-e8bd-466e-b4e7-9047e0ad607f]: STATE TRY_TO_ANSWER_WITH_LOCAL_INFORMATION -> DOUBLE_CHECK_SEND_STATE\n" +
                        "2022-04-01T21:29:00.079-0500 DEBUG DoubleCheckSendState - Skipping double-check - no upstream configured.\n" +
                        "2022-04-01T21:29:00.080-0500 DEBUG RecursiveResolverTask - [f7ec0429-e8bd-466e-b4e7-9047e0ad607f]: STATE DOUBLE_CHECK_SEND_STATE -> SEND_RESPONSE_STATE\n" +
                        "2022-04-01T21:29:00.080-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_OUT\",\"eventTime\":\"2022-04-02T02:29:00.080452Z\",\"id\":\"f7ec0429-e8bd-466e-b4e7-9047e0ad607f\",\"rCode\":0}\n" +
                        "2022-04-01T21:29:00.448-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_IN\",\"eventTime\":\"2022-04-02T02:29:00.448054Z\",\"id\":\"766a2e8e-ccc2-4415-ab7a-277f648c39bd\",\"class\":\"UDPRequest\",\"numQuestions\":1,\"questions\":[{\"qname\":\"google.com\",\"qtype\":1,\"qclass\":1}]}\n" +
                        "2022-04-01T21:29:00.448-0500 INFO RecursiveResolver - [Q] [127.0.0.1]: QNAME: google.com, QTYPE: A, QCLASS: IN | 766a2e8e-ccc2-4415-ab7a-277f648c39bd\n" +
                        "2022-04-01T21:29:00.448-0500 DEBUG RecursiveResolverTask - [766a2e8e-ccc2-4415-ab7a-277f648c39bd]: STATE INITIAL_CHECKING -> SNAME_CHECKING_STATE\n" +
                        "2022-04-01T21:29:00.448-0500 DEBUG RecursiveResolverTask - [766a2e8e-ccc2-4415-ab7a-277f648c39bd]: STATE SNAME_CHECKING_STATE -> TRY_TO_ANSWER_WITH_LOCAL_INFORMATION\n" +
                        "2022-04-01T21:29:00.451-0500 DEBUG RecursiveResolverTask - [766a2e8e-ccc2-4415-ab7a-277f648c39bd]: STATE TRY_TO_ANSWER_WITH_LOCAL_INFORMATION -> DOUBLE_CHECK_SEND_STATE\n" +
                        "2022-04-01T21:29:00.451-0500 DEBUG DoubleCheckSendState - Skipping double-check - no upstream configured.\n" +
                        "2022-04-01T21:29:00.451-0500 DEBUG RecursiveResolverTask - [766a2e8e-ccc2-4415-ab7a-277f648c39bd]: STATE DOUBLE_CHECK_SEND_STATE -> SEND_RESPONSE_STATE\n" +
                        "2022-04-01T21:29:00.452-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_OUT\",\"eventTime\":\"2022-04-02T02:29:00.452095Z\",\"id\":\"766a2e8e-ccc2-4415-ab7a-277f648c39bd\",\"rCode\":0}\n" +
                        "2022-04-01T21:29:00.678-0500 DEBUG TaskDispatcher - No tasks to start\n" +
                        "2022-04-01T21:29:00.781-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_IN\",\"eventTime\":\"2022-04-02T02:29:00.781416Z\",\"id\":\"23b519cf-88b9-4619-a35a-ef77360115d7\",\"class\":\"UDPRequest\",\"numQuestions\":1,\"questions\":[{\"qname\":\"google.com\",\"qtype\":1,\"qclass\":1}]}\n" +
                        "2022-04-01T21:29:00.782-0500 INFO RecursiveResolver - [Q] [127.0.0.1]: QNAME: google.com, QTYPE: A, QCLASS: IN | 23b519cf-88b9-4619-a35a-ef77360115d7\n" +
                        "2022-04-01T21:29:00.782-0500 DEBUG RecursiveResolverTask - [23b519cf-88b9-4619-a35a-ef77360115d7]: STATE INITIAL_CHECKING -> SNAME_CHECKING_STATE\n" +
                        "2022-04-01T21:29:00.782-0500 DEBUG RecursiveResolverTask - [23b519cf-88b9-4619-a35a-ef77360115d7]: STATE SNAME_CHECKING_STATE -> TRY_TO_ANSWER_WITH_LOCAL_INFORMATION\n" +
                        "2022-04-01T21:29:00.784-0500 DEBUG RecursiveResolverTask - [23b519cf-88b9-4619-a35a-ef77360115d7]: STATE TRY_TO_ANSWER_WITH_LOCAL_INFORMATION -> DOUBLE_CHECK_SEND_STATE\n" +
                        "2022-04-01T21:29:00.784-0500 DEBUG DoubleCheckSendState - Skipping double-check - no upstream configured.\n" +
                        "2022-04-01T21:29:00.784-0500 DEBUG RecursiveResolverTask - [23b519cf-88b9-4619-a35a-ef77360115d7]: STATE DOUBLE_CHECK_SEND_STATE -> SEND_RESPONSE_STATE\n" +
                        "2022-04-01T21:29:00.785-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_OUT\",\"eventTime\":\"2022-04-02T02:29:00.785301Z\",\"id\":\"23b519cf-88b9-4619-a35a-ef77360115d7\",\"rCode\":0}\n" +
                        "2022-04-01T21:29:01.129-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_IN\",\"eventTime\":\"2022-04-02T02:29:01.129612Z\",\"id\":\"5f97daed-ab55-4712-9c14-498343b15451\",\"class\":\"UDPRequest\",\"numQuestions\":1,\"questions\":[{\"qname\":\"google.com\",\"qtype\":1,\"qclass\":1}]}\n" +
                        "2022-04-01T21:29:01.130-0500 INFO RecursiveResolver - [Q] [127.0.0.1]: QNAME: google.com, QTYPE: A, QCLASS: IN | 5f97daed-ab55-4712-9c14-498343b15451\n" +
                        "2022-04-01T21:29:01.130-0500 DEBUG RecursiveResolverTask - [5f97daed-ab55-4712-9c14-498343b15451]: STATE INITIAL_CHECKING -> SNAME_CHECKING_STATE\n" +
                        "2022-04-01T21:29:01.130-0500 DEBUG RecursiveResolverTask - [5f97daed-ab55-4712-9c14-498343b15451]: STATE SNAME_CHECKING_STATE -> TRY_TO_ANSWER_WITH_LOCAL_INFORMATION\n" +
                        "2022-04-01T21:29:01.132-0500 DEBUG RecursiveResolverTask - [5f97daed-ab55-4712-9c14-498343b15451]: STATE TRY_TO_ANSWER_WITH_LOCAL_INFORMATION -> DOUBLE_CHECK_SEND_STATE\n" +
                        "2022-04-01T21:29:01.132-0500 DEBUG DoubleCheckSendState - Skipping double-check - no upstream configured.\n" +
                        "2022-04-01T21:29:01.132-0500 DEBUG RecursiveResolverTask - [5f97daed-ab55-4712-9c14-498343b15451]: STATE DOUBLE_CHECK_SEND_STATE -> SEND_RESPONSE_STATE\n" +
                        "2022-04-01T21:29:01.587-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_IN\",\"eventTime\":\"2022-04-02T02:29:01.587132Z\",\"id\":\"a3b866d0-7ec5-4c8a-bcb7-3dfc454ef132\",\"class\":\"UDPRequest\",\"numQuestions\":1,\"questions\":[{\"qname\":\"google.com\",\"qtype\":1,\"qclass\":1}]}\n" +
                        "2022-04-01T21:29:01.587-0500 INFO RecursiveResolver - [Q] [127.0.0.1]: QNAME: google.com, QTYPE: A, QCLASS: IN | a3b866d0-7ec5-4c8a-bcb7-3dfc454ef132\n" +
                        "2022-04-01T21:29:01.588-0500 DEBUG RecursiveResolverTask - [a3b866d0-7ec5-4c8a-bcb7-3dfc454ef132]: STATE INITIAL_CHECKING -> SNAME_CHECKING_STATE\n" +
                        "2022-04-01T21:29:01.588-0500 DEBUG RecursiveResolverTask - [a3b866d0-7ec5-4c8a-bcb7-3dfc454ef132]: STATE SNAME_CHECKING_STATE -> TRY_TO_ANSWER_WITH_LOCAL_INFORMATION\n" +
                        "2022-04-01T21:29:01.591-0500 DEBUG RecursiveResolverTask - [a3b866d0-7ec5-4c8a-bcb7-3dfc454ef132]: STATE TRY_TO_ANSWER_WITH_LOCAL_INFORMATION -> DOUBLE_CHECK_SEND_STATE\n" +
                        "2022-04-01T21:29:01.592-0500 DEBUG DoubleCheckSendState - Skipping double-check - no upstream configured.\n" +
                        "2022-04-01T21:29:01.592-0500 DEBUG RecursiveResolverTask - [a3b866d0-7ec5-4c8a-bcb7-3dfc454ef132]: STATE DOUBLE_CHECK_SEND_STATE -> SEND_RESPONSE_STATE\n" +
                        "2022-04-01T21:29:01.592-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_OUT\",\"eventTime\":\"2022-04-02T02:29:01.592529Z\",\"id\":\"a3b866d0-7ec5-4c8a-bcb7-3dfc454ef132\",\"rCode\":0}\n" +
                        "2022-04-01T21:29:01.680-0500 DEBUG TaskDispatcher - No tasks to start\n" +
                        "2022-04-01T21:29:01.876-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_IN\",\"eventTime\":\"2022-04-02T02:29:01.875826Z\",\"id\":\"6d8237bf-dd0d-419e-b443-e7bac5b86ee1\",\"class\":\"UDPRequest\",\"numQuestions\":1,\"questions\":[{\"qname\":\"google.com\",\"qtype\":1,\"qclass\":1}]}\n" +
                        "2022-04-01T21:29:01.876-0500 INFO RecursiveResolver - [Q] [127.0.0.1]: QNAME: google.com, QTYPE: A, QCLASS: IN | 6d8237bf-dd0d-419e-b443-e7bac5b86ee1\n" +
                        "2022-04-01T21:29:01.876-0500 DEBUG RecursiveResolverTask - [6d8237bf-dd0d-419e-b443-e7bac5b86ee1]: STATE INITIAL_CHECKING -> SNAME_CHECKING_STATE\n" +
                        "2022-04-01T21:29:01.876-0500 DEBUG RecursiveResolverTask - [6d8237bf-dd0d-419e-b443-e7bac5b86ee1]: STATE SNAME_CHECKING_STATE -> TRY_TO_ANSWER_WITH_LOCAL_INFORMATION\n" +
                        "2022-04-01T21:29:01.879-0500 DEBUG RecursiveResolverTask - [6d8237bf-dd0d-419e-b443-e7bac5b86ee1]: STATE TRY_TO_ANSWER_WITH_LOCAL_INFORMATION -> DOUBLE_CHECK_SEND_STATE\n" +
                        "2022-04-01T21:29:01.879-0500 DEBUG DoubleCheckSendState - Skipping double-check - no upstream configured.\n" +
                        "2022-04-01T21:29:01.879-0500 DEBUG RecursiveResolverTask - [6d8237bf-dd0d-419e-b443-e7bac5b86ee1]: STATE DOUBLE_CHECK_SEND_STATE -> SEND_RESPONSE_STATE\n" +
                        "2022-04-01T21:29:01.880-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_OUT\",\"eventTime\":\"2022-04-02T02:29:01.879927Z\",\"id\":\"6d8237bf-dd0d-419e-b443-e7bac5b86ee1\",\"rCode\":0}\n" +
                        "2022-04-01T21:29:02.158-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_IN\",\"eventTime\":\"2022-04-02T02:29:02.158317Z\",\"id\":\"54a35ba5-52e6-449f-a1af-3e22f87d7cb1\",\"class\":\"UDPRequest\",\"numQuestions\":1,\"questions\":[{\"qname\":\"google.com\",\"qtype\":1,\"qclass\":1}]}\n" +
                        "2022-04-01T21:29:02.158-0500 INFO RecursiveResolver - [Q] [127.0.0.1]: QNAME: google.com, QTYPE: A, QCLASS: IN | 54a35ba5-52e6-449f-a1af-3e22f87d7cb1\n" +
                        "2022-04-01T21:29:02.159-0500 DEBUG RecursiveResolverTask - [54a35ba5-52e6-449f-a1af-3e22f87d7cb1]: STATE INITIAL_CHECKING -> SNAME_CHECKING_STATE\n" +
                        "2022-04-01T21:29:02.159-0500 DEBUG RecursiveResolverTask - [54a35ba5-52e6-449f-a1af-3e22f87d7cb1]: STATE SNAME_CHECKING_STATE -> TRY_TO_ANSWER_WITH_LOCAL_INFORMATION\n" +
                        "2022-04-01T21:29:02.161-0500 DEBUG RecursiveResolverTask - [54a35ba5-52e6-449f-a1af-3e22f87d7cb1]: STATE TRY_TO_ANSWER_WITH_LOCAL_INFORMATION -> DOUBLE_CHECK_SEND_STATE\n" +
                        "2022-04-01T21:29:02.161-0500 DEBUG DoubleCheckSendState - Skipping double-check - no upstream configured.\n" +
                        "2022-04-01T21:29:02.161-0500 DEBUG RecursiveResolverTask - [54a35ba5-52e6-449f-a1af-3e22f87d7cb1]: STATE DOUBLE_CHECK_SEND_STATE -> SEND_RESPONSE_STATE\n" +
                        "2022-04-01T21:29:02.161-0500 INFO EventLogger - [EVENT]: {\"eventType\":\"REQUEST_OUT\",\"eventTime\":\"2022-04-02T02:29:02.161691Z\",\"id\":\"54a35ba5-52e6-449f-a1af-3e22f87d7cb1\",\"rCode\":0}\n" +
                        "2022-04-01T21:29:02.682-0500 DEBUG TaskDispatcher - No tasks to start\n" +
                        "2022-04-01T21:29:03.684-0500 DEBUG TaskDispatcher - No tasks to start\n" +
                        "2022-04-01T21:29:04.686-0500 DEBUG TaskDispatcher - No tasks to start\n"
        );

        try(BufferedReader reader = new BufferedReader(r)) {
            ComfyLogProcessor.processStream(reader);
        }
    }
}
