package io.switchbit;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.springframework.cloud.dataflow.rest.client.ApplicationGroupOperations;
import org.springframework.cloud.dataflow.rest.client.DataFlowTemplate;
import org.springframework.cloud.dataflow.rest.resource.ApplicationGroupDefinitionResource;
import org.springframework.cloud.deployer.spi.app.DeploymentState;
import org.springframework.web.client.RestTemplate;

@Mojo(name = "deploy")
@Execute(phase = LifecyclePhase.INSTALL)
public class ApplicationGroupApplicationMojo extends DeployerServerMojo {

	@Parameter(property = "descriptorFile", defaultValue = "${project.build.outputDirectory}/application-group.yml")
	private File applicationGroupDescriptor;

	@Override
	protected DeploymentState getDeploymentState(DataFlowTemplate dataFlowTemplate, String name) {
		ApplicationGroupDefinitionResource applicationGroupDefinitionResource = dataFlowTemplate
				.applicationGroupOperations().display(name);
		return DeploymentState.valueOf(applicationGroupDefinitionResource.getStatus());
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		if (isSkip()) {
			getLog().info("Skipping application group application deployment");
			return;
		}

		if (!applicationGroupDescriptor.exists()) {
			getLog().warn(String.format(
					"This project does not look like an application group. It is missing an expected descriptor '%s'",
					applicationGroupDescriptor.getName()));
		}

		getLog().info("Importing application group: " + getName());
		RestTemplate restTemplate = new RestTemplate();
		DataFlowTemplate dataFlowTemplate;
		try {
			dataFlowTemplate = new DataFlowTemplate(new URI(getDeployerServerUri()), restTemplate);
		}
		catch (URISyntaxException e) {
			throw new MojoExecutionException("Invalid deployer server URI", e);
		}

		String mavenCoordinates = String.format("%s:%s:%s", getMavenProject().getGroupId(),
				getMavenProject().getArtifactId(), getMavenProject().getVersion());
		String uri = String.format("maven://%s", mavenCoordinates);

		validateFeatures(dataFlowTemplate);
		importApplicationGroup(dataFlowTemplate.applicationGroupOperations(), uri);

		if (isWaitForDeployment()) {
			waitForDeployment(dataFlowTemplate, getName(), getMaxAttempts(), getInterval());
		}
	}

	private void importApplicationGroup(ApplicationGroupOperations applicationGroupOperations, String uri) {
		applicationGroupOperations.importApplicationGroup(getName(), uri, true, true, getDeploymentProperties());
	}

	private void validateFeatures(DataFlowTemplate dataFlowTemplate) {
		if (dataFlowTemplate.applicationGroupOperations() == null) {
			throw new FeatureNotAvailableException("'%s' feature not available on deployer server at: '%s'",
					ApplicationGroupOperations.class.getName(), getDeployerServerUri());
		}
	}
}
