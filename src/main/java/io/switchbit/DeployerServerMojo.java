package io.switchbit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.springframework.cloud.dataflow.rest.client.DataFlowTemplate;
import org.springframework.cloud.deployer.spi.app.DeploymentState;
import org.springframework.util.StringUtils;

public abstract class DeployerServerMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject mavenProject;

	@Parameter(property = "skip", defaultValue = "false")
	private boolean skip;

	@Parameter(property = "deployerServerUri", defaultValue = "http://localhost:9393", required = true)
	private String deployerServerUri;

	@Parameter(property = "name", defaultValue = "${project.name}", required = true)
	private String name;

	@Parameter(property = "wait", defaultValue = "true")
	private boolean waitForDeployment;

	@Parameter(property = "interval", defaultValue = "30")
	private int interval;

	@Parameter(property = "maxAttempts", defaultValue = "10")
	private int maxAttempts;

	@Parameter
	private Map<String, String> applicationProperties = new HashMap<>();

	@Parameter
	private Map<String, String> deploymentProperties = new HashMap<>();

	protected String applicationPropertiesToString(
			Map<String, String> applicationProperties) {
		List<String> properties = new ArrayList<>();
		for (Map.Entry<String, String> propertyEntry : applicationProperties.entrySet()) {
			properties.add(String.format("--%s=%s", propertyEntry.getKey(),
					propertyEntry.getValue()));
		}

		return StringUtils.collectionToDelimitedString(properties, " ");
	}

	protected void waitForDeployment(DataFlowTemplate dataFlowTemplate, String name, int maxAttempts, int interval) {
		getLog().info("Waiting for application to deploy...");
		int attempt = 0;
		DeploymentState status;
		do {
			try {
				Thread.sleep(interval * 1000);
			}
			catch (InterruptedException e) {
				throw new RuntimeException(
						"Waiting for application to deploy interrupted");
			}

			attempt++;
			status = getDeploymentState(dataFlowTemplate, name);
			getLog().info(String.format(
					"Waiting for application to be deployed, currently '%s'", status));
		}
		while ((status.equals(DeploymentState.deploying)
				|| status.equals(DeploymentState.undeployed)) && attempt < maxAttempts);

		if (status.equals(DeploymentState.deployed)) {
			getLog().info(String.format("Application '%s' deployed successfully", name));
		} else {
			getLog().error(String.format("Application '%s' deployment failed", name));
			throw new DeploymentFailedException("Application '%s' deployment failed", name);
		}
	}

	protected abstract DeploymentState getDeploymentState(DataFlowTemplate dataFlowTemplate, String name);

	public MavenProject getMavenProject() {
		return mavenProject;
	}

	public boolean isSkip() {
		return skip;
	}

	public String getDeployerServerUri() {
		return deployerServerUri;
	}

	public String getName() {
		return name;
	}

	public boolean isWaitForDeployment() {
		return waitForDeployment;
	}

	public int getInterval() {
		return interval;
	}

	public int getMaxAttempts() {
		return maxAttempts;
	}

	public Map<String, String> getApplicationProperties() {
		return applicationProperties;
	}

	public Map<String, String> getDeploymentProperties() {
		return deploymentProperties;
	}
}
