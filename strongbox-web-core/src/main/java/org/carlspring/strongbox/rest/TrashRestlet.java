package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.resolvers.ArtifactStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;

/**
 * @author Martin Todorov
 */
@Component
@Path("/trash")
public class TrashRestlet
        extends BaseArtifactRestlet
{

    private static final Logger logger = LoggerFactory.getLogger(TrashRestlet.class);

    @Autowired
    private ArtifactManagementService artifactManagementService;


    @DELETE
    @Path("{storageId}/{repositoryId}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response delete(@PathParam("storageId") String storageId,
                           @PathParam("repositoryId") String repositoryId)
            throws IOException
    {
        if (getStorage(storageId) == null)
        {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("The specified storageId does not exist!")
                           .build();
        }
        if (getRepository(storageId, repositoryId) == null)
        {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("The specified repositoryId does not exist!")
                           .build();
        }

        try
        {
            artifactManagementService.deleteTrash(storageId, repositoryId);

            logger.debug("Deleted trash for repository " + repositoryId + ".");
        }
        catch (ArtifactStorageException e)
        {
                return Response.status(Response.Status.BAD_REQUEST)
                               .entity(e.getMessage())
                               .build();
        }

        return Response.ok().build();
    }

    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    public Response delete()
            throws IOException
    {
        try
        {
            artifactManagementService.deleteTrash();

            logger.debug("Deleted trash for all repositories.");
        }
        catch (ArtifactStorageException e)
        {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }

        return Response.ok().build();
    }

    @POST
    @Path("{storageId}/{repositoryId}/{path:.*}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response undelete(@PathParam("storageId") String storageId,
                             @PathParam("repositoryId") String repositoryId,
                             @PathParam("path") String path)
            throws IOException
    {
        logger.debug("UNDELETE: " + path);
        logger.debug(storageId + ":" + repositoryId + ": " + path);

        if (getStorage(storageId) == null)
        {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("The specified storageId does not exist!")
                           .build();
        }
        if (getRepository(storageId, repositoryId) == null)
        {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("The specified repositoryId does not exist!")
                           .build();
        }

        if (!new File(getRepository(storageId, repositoryId).getBasedir() + "/.trash", path).exists())
        {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("The specified path does not exist!")
                           .build();
        }

        try
        {
            artifactManagementService.undelete(storageId, repositoryId, path);

            logger.debug("Undeleted trash for path " + path + " under repository " + storageId + ":" + repositoryId + ".");
        }
        catch (ArtifactStorageException e)
        {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(e.getMessage())
                           .build();
        }

        return Response.ok().build();
    }

    @POST
    @Path("{storageId}/{repositoryId}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response undelete(@PathParam("storageId") String storageId,
                             @PathParam("repositoryId") String repositoryId)
            throws IOException
    {
        if (getConfiguration().getStorage(storageId).getRepository(repositoryId) != null)
        {
            try
            {
                artifactManagementService.undeleteTrash(storageId, repositoryId);

                logger.debug("Undeleted trash for repository " + repositoryId + ".");
            }
            catch (ArtifactStorageException e)
            {
                if (artifactManagementService.getStorage(storageId) == null)
                {
                    return Response.status(Response.Status.NOT_FOUND)
                                   .entity("The specified storageId does not exist!")
                                   .build();
                }
                else if (artifactManagementService.getStorage(storageId).getRepository(repositoryId) == null)
                {
                    return Response.status(Response.Status.NOT_FOUND)
                                   .entity("The specified repositoryId does not exist!")
                                   .build();
                }

                return Response.status(Response.Status.BAD_REQUEST)
                               .entity(e.getMessage())
                               .build();
            }

            return Response.ok().build();
        }
        else
        {
            return Response.status(Response.Status.NOT_FOUND).entity("Storage or repository could not be found!").build();
        }
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Response undelete()
            throws IOException
    {
        try
        {
            artifactManagementService.undeleteTrash();

            logger.debug("Undeleted trash for all repositories.");
        }
        catch (ArtifactStorageException e)
        {
            logger.error(e.getMessage(), e);

            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(e.getMessage())
                           .build();
        }

        return Response.ok().build();
    }

}
