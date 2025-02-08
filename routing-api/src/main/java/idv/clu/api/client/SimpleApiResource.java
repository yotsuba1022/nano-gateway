package idv.clu.api.client;

/**
 * @author clu
 */
public enum SimpleApiResource {

    BASE_URL("/simple-api"),
    REST_RESOURCE("rest_resource"),
    ECHO_ENDPOINT("echo");

    private final String value;

    SimpleApiResource(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    static String buildPath(SimpleApiResource... resources) {
        StringBuilder pathBuilder = new StringBuilder();
        for (SimpleApiResource resource : resources) {
            if (!pathBuilder.isEmpty() && !pathBuilder.toString().endsWith("/")) {
                pathBuilder.append("/");
            }
            pathBuilder.append(resource.getValue());
        }
        return pathBuilder.toString();
    }

    public static String getSimpleApiEchoUrl() {
        return SimpleApiResource.buildPath(
                SimpleApiResource.BASE_URL,
                SimpleApiResource.REST_RESOURCE,
                SimpleApiResource.ECHO_ENDPOINT
        );
    }

}
