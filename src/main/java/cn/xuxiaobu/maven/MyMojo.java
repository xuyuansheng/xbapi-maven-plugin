package cn.xuxiaobu.maven;

import cn.xuxiaobu.doc.AbstractJavaProcessSynopsis;
import cn.xuxiaobu.doc.MavenJavaProcessSynopsis;
import cn.xuxiaobu.doc.config.JavaConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.plugins.dependency.resolvers.ResolveDependenciesMojo;
import org.apache.maven.plugins.dependency.utils.DependencyStatusSets;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Goal which touches a timestamp file.
 *
 * @author xuyuansheng
 */
@Mojo(name = "rest-api", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.TEST, threadSafe = true)
public class MyMojo extends ResolveDependenciesMojo {
    /**
     * 文档输出目录
     */
    @Parameter
    String xbOutPutDir;


    /**
     * 协议
     */
    @Parameter
    String protocol;

    /**
     * 域名
     */
    @Parameter
    String host;

    /**
     * 端口
     */
    @Parameter
    String port;

    private static final String SOURCE_CLASSIFIER = "sources";

    @Override
    protected void doExecute() throws MojoExecutionException {

        MavenProject project = getProject();
        String javaSource = project.getBuild().getSourceDirectory();
        String classSource = project.getBuild().getOutputDirectory();
        String targetApis = project.getBuild().getDirectory() + File.separator + "xbApis";

        if (StringUtils.isNotBlank(xbOutPutDir)) {
            Path path = Paths.get(xbOutPutDir);
            if (path.isAbsolute()) {
                path = Paths.get(project.getBuild().getDirectory(), path.toString());
            }
            if (!Files.exists(path) || !Files.isDirectory(path)) {
                path.toFile().mkdirs();
            }
            targetApis = path.toString();
        }


        List<String> classDependencySource = getDependency(true);
        List<String> javaDependencySource = classDependencySource.stream().map(de -> StringUtils.replace(de, ".jar", "-sources.jar"))
                .filter(sourceDe -> Paths.get(sourceDe).toFile().exists()).collect(Collectors.toList());
        JavaConfig javaConfig = new JavaConfig(javaSource, classSource, javaDependencySource, classDependencySource)
                .setOutPutDir(targetApis)
                .setProtocol(protocol)
                .setPort(port)
                .setHost(host);
        AbstractJavaProcessSynopsis javaProcessSynopsis = new MavenJavaProcessSynopsis(javaConfig);
        javaProcessSynopsis.buildDoc();
        getLog().info(javaDependencySource.toString());

    }

    public List<String> getDependency(Boolean ifClasses) throws MojoExecutionException {
        this.classifier = ifClasses ? "" : SOURCE_CLASSIFIER;
        DependencyStatusSets dss = getDependencySets(false, false);
        Set<Artifact> artifacts = dss.getResolvedDependencies();
        return artifacts.stream().map(k -> k.getFile().getAbsolutePath()).collect(Collectors.toList());
    }

}
