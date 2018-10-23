package org.carlspring.strongbox.providers.io;

import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.repository.event.RepositoryPathExpiredEvent;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class MavenRepositoryPathExpiredEventListener
{

    private static final Logger logger = LoggerFactory.getLogger(MavenRepositoryPathExpiredEventListener.class);

    @Inject
    private List<MavenExpiredRepositoryPathHandler> expiredRepositoryPathHandlers;

    @EventListener
    public void handle(final RepositoryPathExpiredEvent event)
    {

        RepositoryPath repositoryPath = event.getPath();
        if (!Maven2LayoutProvider.ALIAS.equals(repositoryPath.getRepository().getLayout()))
        {
            return;
        }

        expiredRepositoryPathHandlers.stream()
                                     .filter(handler -> handler.supports(repositoryPath))
                                     .forEach(handleExpiration(repositoryPath));
    }

    private Consumer<MavenExpiredRepositoryPathHandler> handleExpiration(final RepositoryPath repositoryPath)
    {
        return handler ->
        {
            try
            {
                handler.handleExpiration(repositoryPath);
            }
            catch (IOException e)
            {
                logger.error(String.format("Expired path [%s] inproperly handled.", repositoryPath), e);
            }
        };
    }
}
