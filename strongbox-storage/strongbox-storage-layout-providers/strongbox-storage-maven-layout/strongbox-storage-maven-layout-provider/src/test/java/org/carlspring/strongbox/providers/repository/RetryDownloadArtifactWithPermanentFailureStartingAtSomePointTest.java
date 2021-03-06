package org.carlspring.strongbox.providers.repository;

import org.carlspring.strongbox.artifact.MavenArtifactUtils;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.MavenArtifactTestUtils;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Remote;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.apache.maven.artifact.Artifact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.Resource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author Przemyslaw Fusik
 */
public class RetryDownloadArtifactWithPermanentFailureStartingAtSomePointTest
        extends MockedRestArtifactResolverTestBase
        implements ArtifactResolverContext
{

    private static final String REPOSITORY = "rdawpfsasp-repository";

    private static final String PROXY_REPOSITORY_URL = "https://repo.maven.apache.org/maven2/";

    private PermanentBrokenArtifactInputStream brokenArtifactInputStream;

    @Override
    public InputStream getInputStream()
    {
        return brokenArtifactInputStream;
    }

    @Override
    protected ArtifactResolverContext lookupArtifactResolverContext()
    {
        return this;
    }

    @Override
    @BeforeEach
    public void setup()
            throws IOException
    {
        super.setup();

        brokenArtifactInputStream = new PermanentBrokenArtifactInputStream(jarArtifact);
    }

    @Test
    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    public void whenProxyRepositoryInputStreamFailsCompletelyArtifactDownloadShouldFail(@MavenRepository(repositoryId = REPOSITORY)
                                                                                        @Remote(url = PROXY_REPOSITORY_URL)
                                                                                        Repository proxyRepository)
    {
        final String storageId = proxyRepository.getStorage().getId();
        final String repositoryId = proxyRepository.getId();


        Artifact artifact = MavenArtifactTestUtils.getArtifactFromGAVTC("org.apache.commons:commons-lang3:3.1");
        String path = MavenArtifactUtils.convertArtifactToPath(artifact);
        RepositoryPath artifactPath = repositoryPathResolver.resolve(proxyRepository,
                                                                     path);

        // given
        assertThat(Files.exists(artifactPath)).isFalse();

        assertThatExceptionOfType(IOException.class)
                .isThrownBy(() -> artifactResolutionServiceHelper.assertStreamNotNull(storageId,
                                                                      repositoryId,
                                                                      path)
                ).withMessageMatching("Connection lost.");
    }

    static class PermanentBrokenArtifactInputStream
            extends MockedRestArtifactResolverTestBase.BrokenArtifactInputStream
    {

        private int currentReadSize;

        public PermanentBrokenArtifactInputStream(final Resource jarArtifact)
        {
            super(jarArtifact);
        }

        @Override
        public int read()
                throws IOException
        {

            if (currentReadSize >= BUF_SIZE)
            {
                throw new IOException("Connection lost.");
            }

            currentReadSize++;
            return artifactInputStream.read();
        }

    }
}

