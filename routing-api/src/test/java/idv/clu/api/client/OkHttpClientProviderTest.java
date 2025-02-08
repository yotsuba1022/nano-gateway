package idv.clu.api.client;

import idv.clu.api.circuitbreaker.CircuitBreaker;
import idv.clu.api.strategy.retry.RetryStrategy;
import idv.clu.api.strategy.routing.RoutingStrategy;
import jakarta.ws.rs.core.Response;
import okhttp3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("OkHttpClientProvider Test Suite")
class OkHttpClientProviderTest {

    private final static MediaType APPLICATION_JSON = MediaType.get(jakarta.ws.rs.core.MediaType.APPLICATION_JSON);
    private final static List<String> MOCK_INSTANCES =
            List.of("http://example.com", "http://example2.com", "http://example3.com");
    private final static String ROUTING_PATH = SimpleApiResource.getSimpleApiEchoUrl();
    private final static String MOCK_VALID_REQUEST_PAYLOAD = "{\"game\": \"Mobile Legends\", \"gamerID\": \"GYUTDTE\", \"points\": 20}";
    private final static String MOCK_INVALID_REQUEST_PAYLOAD = "{\"invalid\": \"Mobile Legends\"\"}";

    @Mock
    private OkHttpClient okHttpClient;

    @Mock
    private RetryStrategy retryStrategy;

    @Mock
    private RoutingStrategy routingStrategy;

    @Mock
    private CircuitBreaker circuitBreaker;

    @InjectMocks
    private OkHttpClientProvider okHttpClientProvider;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        okHttpClientProvider.clientTimeoutThreshold = 100000L;

        when(retryStrategy.executeWithRetries(any()))
                .thenAnswer(invocation -> {
                    Callable<Response> callable = invocation.getArgument(0);
                    return callable.call();
                });

        when(routingStrategy.getNextTargetUrl(anyString()))
                .thenAnswer(invocation -> MOCK_INSTANCES.get(0) + ROUTING_PATH);

        when(circuitBreaker.allowRequest(anyString())).thenReturn(true);
    }

    @Test
    @DisplayName("Test successful execution of a GET request")
    void testSendGetRequestWithSuccessful() throws Exception {
        Request request = new Request.Builder()
                .url(MOCK_INSTANCES.get(0))
                .build();
        okhttp3.Response realResponse = new okhttp3.Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create(MOCK_VALID_REQUEST_PAYLOAD, APPLICATION_JSON))
                .build();

        Call callMock = mock(Call.class);
        when(okHttpClient.newCall(any(Request.class))).thenReturn(callMock);
        when(callMock.execute()).thenReturn(realResponse);

        Response response = okHttpClientProvider.sendGetRequest(ROUTING_PATH);

        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertEquals(MOCK_VALID_REQUEST_PAYLOAD, response.getEntity().toString());

        verify(okHttpClient, times(1)).newCall(any(Request.class));
        verify(callMock, times(1)).execute();
        verify(routingStrategy, times(1)).getNextTargetUrl(ROUTING_PATH);
        verify(circuitBreaker, times(1)).allowRequest(MOCK_INSTANCES.get(0) + ROUTING_PATH);
        verify(circuitBreaker, times(1)).reportSuccess(MOCK_INSTANCES.get(0) + ROUTING_PATH);
        verify(circuitBreaker, times(0)).reportFailure(anyString());
    }

    @Test
    @DisplayName("Test GET request with a failed response")
    void testSendGetRequestWithError() throws Exception {
        Request request = new Request.Builder()
                .url(MOCK_INSTANCES.get(0))
                .build();
        okhttp3.Response realResponse = new okhttp3.Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(500)
                .message("Internal Server Error")
                .body(ResponseBody.create("Error occurred", APPLICATION_JSON))
                .build();

        Call callMock = mock(Call.class);
        when(okHttpClient.newCall(any(Request.class))).thenReturn(callMock);
        when(callMock.execute()).thenReturn(realResponse);

        Response response = okHttpClientProvider.sendGetRequest(ROUTING_PATH);

        assertNotNull(response);
        assertEquals(500, response.getStatus());
        assertEquals("Error occurred", response.getEntity().toString());

        verify(okHttpClient, times(1)).newCall(any(Request.class));
        verify(callMock, times(1)).execute();
        verify(routingStrategy, times(1)).getNextTargetUrl(ROUTING_PATH);
        verify(circuitBreaker, times(1)).allowRequest(MOCK_INSTANCES.get(0) + ROUTING_PATH);
        verify(circuitBreaker, times(1)).reportSuccess(MOCK_INSTANCES.get(0) + ROUTING_PATH);
        verify(circuitBreaker, times(0)).reportFailure(anyString());
    }

    @Test
    @DisplayName("Test successful execution of a POST request")
    void testSendPostRequestWithSuccessful() throws Exception {
        Request request = new Request.Builder()
                .url(MOCK_INSTANCES.get(0))
                .post(RequestBody.create(MOCK_VALID_REQUEST_PAYLOAD, APPLICATION_JSON))
                .build();
        okhttp3.Response realResponse = new okhttp3.Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(201)
                .message("Created")
                .body(ResponseBody.create(MOCK_VALID_REQUEST_PAYLOAD, APPLICATION_JSON))
                .build();

        Call callMock = mock(Call.class);
        when(okHttpClient.newCall(any(Request.class))).thenReturn(callMock);
        when(callMock.execute()).thenReturn(realResponse);

        Response response = okHttpClientProvider.sendPostRequest(ROUTING_PATH, MOCK_VALID_REQUEST_PAYLOAD);

        assertNotNull(response);
        assertEquals(201, response.getStatus());
        assertEquals(MOCK_VALID_REQUEST_PAYLOAD, response.getEntity().toString());

        verify(okHttpClient, times(1)).newCall(any(Request.class));
        verify(callMock, times(1)).execute();
        verify(routingStrategy, times(1)).getNextTargetUrl(ROUTING_PATH);
        verify(circuitBreaker, times(1)).allowRequest(MOCK_INSTANCES.get(0) + ROUTING_PATH);
        verify(circuitBreaker, times(1)).reportSuccess(MOCK_INSTANCES.get(0) + ROUTING_PATH);
        verify(circuitBreaker, times(0)).reportFailure(anyString());
    }

    @Test
    @DisplayName("Test POST request with a failed response")
    void testSendPostRequestWithError() throws Exception {
        Request request = new Request.Builder()
                .url(MOCK_INSTANCES.get(0))
                .post(RequestBody.create(MOCK_INVALID_REQUEST_PAYLOAD, APPLICATION_JSON))
                .build();
        okhttp3.Response realResponse = new okhttp3.Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(400)
                .message("Bad Request")
                .body(ResponseBody.create(MOCK_INVALID_REQUEST_PAYLOAD, APPLICATION_JSON))
                .build();

        Call callMock = mock(Call.class);
        when(okHttpClient.newCall(any(Request.class))).thenReturn(callMock);
        when(callMock.execute()).thenReturn(realResponse);

        Response response = okHttpClientProvider.sendPostRequest(ROUTING_PATH, MOCK_INVALID_REQUEST_PAYLOAD);

        assertNotNull(response);
        assertEquals(400, response.getStatus());
        assertEquals(MOCK_INVALID_REQUEST_PAYLOAD, response.getEntity().toString());

        verify(okHttpClient, times(1)).newCall(any(Request.class));
        verify(callMock, times(1)).execute();
        verify(routingStrategy, times(1)).getNextTargetUrl(ROUTING_PATH);
        verify(circuitBreaker, times(1)).allowRequest(MOCK_INSTANCES.get(0) + ROUTING_PATH);
        verify(circuitBreaker, times(1)).reportSuccess(MOCK_INSTANCES.get(0) + ROUTING_PATH);
        verify(circuitBreaker, times(0)).reportFailure(anyString());
    }

}
