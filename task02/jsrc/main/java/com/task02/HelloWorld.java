package com.task02;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.Architecture;
import com.syndicate.deployment.model.ArtifactExtension;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import java.util.HashMap;
import java.util.Map;

@LambdaHandler(
		lambdaName = "hello_world",
		roleName = "hello_world-role",
		isPublishVersion = false,
		layers = {"sdk-layer"},
		runtime = DeploymentRuntime.JAVA11,
		architecture = Architecture.ARM64,
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaLayer(
		layerName = "sdk-layer",
		libraries = {"lib/commons-lang3-3.14.0.jar", "lib/gson-2.10.1.jar"},
		runtime = DeploymentRuntime.JAVA11,
		architectures = {Architecture.ARM64},
		artifactExtension = ArtifactExtension.ZIP
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
public class HelloWorld implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private static final Gson gson = new GsonBuilder().create();

	@Override
	public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
		String path = event.getRawPath();
		String httpMethod = event.getRequestContext().getHttp().getMethod();

		if ("/hello".equals(path) && "GET".equalsIgnoreCase(httpMethod)) {
			return sendResponse(200, "Hello from Lambda");
		} else {
			return sendErrorResponse(400,
					"Bad request syntax or unsupported method. Request path: " + path + ". HTTP method: " + httpMethod);
		}
	}

	private APIGatewayV2HTTPResponse sendResponse(int statusCode, String message) {
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", "application/json");

		Map<String, Object> body = new HashMap<>();
		body.put("statusCode", statusCode);
		body.put("message", message);

		String jsonBody = gson.toJson(body);

		return APIGatewayV2HTTPResponse.builder()
				.withStatusCode(statusCode)
				.withHeaders(headers)
				.withBody(jsonBody) // Ensure body is properly serialized to JSON
				.build();
	}

	private APIGatewayV2HTTPResponse sendErrorResponse(int statusCode, String message) {
		return sendResponse(statusCode, message);
	}
}