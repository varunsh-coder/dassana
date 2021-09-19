package app.dassana.core.api;

//import app.dassana.core.contentmanager.infra.S3Manager;
import app.dassana.core.launch.ApiHandler;
import app.dassana.core.launch.App;
import app.dassana.core.launch.Helper;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.*;

import static app.dassana.core.contentmanager.ContentManager.WORKFLOW_ID;

//@Introspected
public class WorkflowTest {

	//@Inject Helper helper;

	/*
	static Map<String,String> getModifiableEnvironment() throws Exception{
		Class pe = Class.forName("java.lang.ProcessEnvironment");
		Method getenv = pe.getDeclaredMethod("getenv");
		getenv.setAccessible(true);
		Object unmodifiableEnvironment = getenv.invoke(null);
		Class map = Class.forName("java.util.Collections$UnmodifiableMap");
		Field m = map.getDeclaredField("m");
		m.setAccessible(true);
		return (Map) m.get(unmodifiableEnvironment);
	}

	@BeforeAll
	static void setEnv()throws Exception{
		Map<String, String> env = getModifiableEnvironment();
		env.put("dassanaBucket", "workflow-dassanabucket-1r7m06ls0ymh7");
		env.put("AWS_ACCESS_KEY_ID", "ASIA3IHACRANFA3XIDNS");
		env.put("AWS_SECRET_ACCESS_KEY", "m/vQRqGX+ig18QsUFPspwRUvkY0OTkkgG3JUCrqp");
		//env.put("AWS_SESSION_TOKEN", "");
		env.put("AWS_SESSION_TOKEN", "IQoJb3JpZ2luX2VjECUaCXVzLXdlc3QtMiJGMEQCIHomY9H2TTpMDVihPdd9uenOu2rXEP5uLHxTXG/3ahSTAiB+ypdIvVC0xaQ2PfJFC4beZSKGzGZhNdr/G0eLaC2HJCqIAwh+EAAaDDc3MzU2NDA0MTI0MiIMT13K8o7Wbp2o4H+0KuUCPM5HBo4Ch+nM0W0nQqr6Yvw7Xzv+OobpBkJisGfVBgqTOz9f5VPYXfO8c8YE3idcWNENPzmcLI/8EAkbp5uLlNWl2BcV2B7IQqPDxouu1q50iqJIAavTdDtMd/Xhe+Yf+IKoQQ9jMaXmZe1CtT+doKtYz1ocKgNQxJerQUjm50lpEN+/rePYBf5cD6/u/wni1IabRYqeUkuDNXS1bf9XYM/k30wRU1TtLDeAa/b+t/V1Vo71cUUlM6R0mMfoaluy02wXaor/HydUQywu1mLFhy6ORniAyv8cNKcWyKYF34EkFom8vQJfBVEaAs9d5mlDM1frkUPotuChw9/UG8ZKK6UJPdWQKkxm+nK7eXD5b29zDS82GH4+c/acH2VCe8sXkFXY2ff3hnbnkRZIVjVn480vh1JL2Fv3QWtOBU8EDYgbEqg0Ixn6KpMhZr7MXEoMQE7y2B31wz+9SweyZCMl3psan7hdMJrhjooGOqcBy42Sn6OErNaDWIKhtgR1lh+X5y6HT0Hj9uB7ycpx2bKEaAgP3Y9orelMhBXh6rUE1pU7/WC2ADxUhb6crbbtUvo7jUEMpOuu++RhJ3HEgwxmpD006TaPmSUSU/tsB4PVyCX41JatAx8TpYYJQ/O/Y+VCo+bDGITtDk2DcGtLwuB61BR4sfjbNODzcO0bHT8TDEZET13TN63E1Vh8GOs4UlvmdlBXymM=");
	}


	@Test
	void s3Download() throws Exception {
		S3Client s3Client = S3Client.builder().httpClient(UrlConnectionHttpClient.builder().build()).build();
		S3Manager s3Downloader = new S3Manager(s3Client);
		Optional<File> optionalFile = s3Downloader.downloadContent(0L);

		Collection<File> files = FileUtils.listFiles(optionalFile.get(), null, false);

		for(File file : files){
			String yamlContent = Files.readString(file.toPath());
			System.out.println(yamlContent);
		}
	}

	@Test
	void s3Delete() throws Exception {
		S3Client s3Client = S3Client.builder().httpClient(UrlConnectionHttpClient.builder().build()).build();
		S3Manager s3Manager = new S3Manager(s3Client);
		String workflowId = "foo-cloud-normalize";
		s3Manager.deleteContent(workflowId);
	}

	@Test
	void getWorkflow() throws Exception {
	Helper helper = new Helper();
	ApiHandler apiHandler = new ApiHandler();
	Map<String, String> queryParams = helper.getQueryParams(false);
		queryParams.put(WORKFLOW_ID, "foo-cloud-normalize");
	APIGatewayProxyResponseEvent apiGatewayProxyResponseEvent = helper.executeRunApiGet(queryParams);
	String body = apiGatewayProxyResponseEvent.getBody();
	}
	*/

}
