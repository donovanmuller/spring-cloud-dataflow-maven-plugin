package io.switchbit;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.springframework.cloud.dataflow.core.ApplicationType;
import org.springframework.cloud.dataflow.rest.client.DataFlowTemplate;
import org.springframework.cloud.dataflow.rest.client.StandaloneOperations;
import org.springframework.cloud.dataflow.rest.resource.StandaloneDefinitionResource;
import org.springframework.cloud.deployer.spi.app.DeploymentState;

@Mojo(name = "deploy-standalone")
@Execute(phase = LifecyclePhase.INSTALL)
public class StandaloneApplicationMojo extends DeployerServerMojo {

	@Override
	protected DeploymentState getDeploymentState(DataFlowTemplate dataFlowTemplate, String name) {
		StandaloneDefinitionResource standaloneDefinitionResource = dataFlowTemplate.standaloneOperations()
				.display(name);
		return DeploymentState.valueOf(standaloneDefinitionResource.getStatus());
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		if (isSkip()) {
			getLog().info("Skipping standalone application deployment");
			return;
		}

		getLog().info("Deploying standalone application: " + getName());

		DataFlowTemplate dataFlowTemplate;
		try {
			dataFlowTemplate = new DataFlowTemplate(new URI(getDeployerServerUri()));
		}
		catch (URISyntaxException e) {
			throw new MojoExecutionException("Invalid deployer server URI", e);
		}

		String mavenCoordinates = String.format("%s:%s:%s", getMavenProject().getGroupId(),
				getMavenProject().getArtifactId(), getMavenProject().getVersion());
		String uri = String.format("maven://%s", mavenCoordinates);

		register(dataFlowTemplate, uri);
		create(dataFlowTemplate.standaloneOperations());
		deploy(dataFlowTemplate.standaloneOperations());

		if (isWaitForDeployment()) {
			waitForDeployment(dataFlowTemplate, getName(), getMaxAttempts(), getInterval());
		}
	}

	private void register(DataFlowTemplate dataFlowTemplate, String uri) {
		dataFlowTemplate.appRegistryOperations().register(getName(), ApplicationType.standalone, uri, true);
	}

	private void create(StandaloneOperations standaloneOperations) {
		standaloneOperations.createStandalone(getName(),
				String.format("%s %s", getName(), applicationPropertiesToString(getApplicationProperties())), true,
				false);
	}

	private void deploy(StandaloneOperations standaloneOperations) {
		getLog().debug("Using deployment properties: " + getDeploymentProperties());
		standaloneOperations.deploy(getName(), getDeploymentProperties(), true);
	}
}
