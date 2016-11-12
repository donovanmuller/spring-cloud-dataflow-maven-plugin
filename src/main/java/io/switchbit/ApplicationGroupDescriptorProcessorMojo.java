package io.switchbit;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.springframework.boot.bind.YamlConfigurationFactory;
import org.springframework.core.io.FileSystemResource;
import org.yaml.snakeyaml.Yaml;

@Mojo(name = "application-group-processor", defaultPhase = LifecyclePhase.PROCESS_RESOURCES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class ApplicationGroupDescriptorProcessorMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject mavenProject;

	@Parameter(property = "descriptorFile", defaultValue = "${project.build.outputDirectory}/application-group.yml")
	private File applicationGroupDescriptor;

	@Parameter(property = "outputDirectory", defaultValue = "${project.build.outputDirectory}")
	private File outputDirectory;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		YamlConfigurationFactory<ApplicationGroupDescriptor> yamlConfigurationFactory = new YamlConfigurationFactory<ApplicationGroupDescriptor>(
				ApplicationGroupDescriptor.class);
		yamlConfigurationFactory.setResource(new FileSystemResource(applicationGroupDescriptor));
		try {
			yamlConfigurationFactory.afterPropertiesSet();
			ApplicationGroupDescriptor descriptor = yamlConfigurationFactory.getObject();

			for (ApplicationGroupDescriptor.Application application : descriptor.getApps()) {
				if (!StringUtils.startsWithAny(application.getUri(), "maven://", "docker://", "" +
					"file://")) {
					getLog().debug(String.format("Processing app '%s' with URI '%s'", application.getName(),
							application.getUri()));

					for (Dependency dependency : mavenProject.getDependencies()) {
						if (dependency.getArtifactId().equals(application.getUri())) {
							String mavenCoordinates = String.format("%s:%s:%s", dependency.getGroupId(),
									dependency.getArtifactId(), dependency.getVersion());
							application.setUri(String.format("maven://%s", mavenCoordinates));

							getLog().debug(String.format("Processed app '%s' URI, resolved as '%s'",
									application.getName(), application.getUri()));
						}
					}

					// no matching dependencies, dependency must be missing
					if (!application.getUri().startsWith("maven://")) {
						String errorMessage = String.format("Application '%s' is not added as a dependency.",
								application.getName());
						getLog().error(errorMessage);
						throw new IllegalArgumentException(errorMessage);
					}
				}
			}

			Yaml yaml = new Yaml();
			Files.write(new File(outputDirectory, applicationGroupDescriptor.getName()).toPath(),
					yaml.dump(descriptor).getBytes(StandardCharsets.UTF_8));
		}
		catch (Exception e) {
			getLog().warn(
					String.format("Could not parse descriptor at '%s'. Cannot populate dependency references. '%s'",
							applicationGroupDescriptor, e.getMessage()));
			throw new RuntimeException(e);
		}
	}
}
