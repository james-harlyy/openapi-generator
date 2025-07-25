/*
 * OpenAPI Petstore
 * This is a sample server Petstore server. For this sample, you can use the api key `special-key` to test the authorization filters.
 *
 * The version of the OpenAPI document: 1.0.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package org.openapitools.client.api;

import java.io.File;
import org.openapitools.client.model.ModelApiResponse;
import org.openapitools.client.model.Pet;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.MediaType;
import org.apache.cxf.jaxrs.ext.multipart.*;
import io.smallrye.mutiny.Uni;


import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * OpenAPI Petstore
 *
 * <p>This is a sample server Petstore server. For this sample, you can use the api key `special-key` to test the authorization filters.
 *
 */

@RegisterRestClient(configKey="petstore")
@RegisterProvider(ApiExceptionMapper.class)
@Path("/pet")
public interface PetApi  {

    /**
     * Add a new pet to the store
     *
     * 
     *
     */
    @POST
    
    @Consumes({ "application/json", "application/xml" })
    @Produces({ "application/xml", "application/json" })
    Uni<Pet> addPet(Pet pet) throws ApiException, ProcessingException;

    /**
     * Deletes a pet
     *
     * 
     *
     */
    @DELETE
    @Path("/{petId}")
    Uni<Void> deletePet(@PathParam("petId") Long petId, @HeaderParam("api_key")  String apiKey) throws ApiException, ProcessingException;

    /**
     * Finds Pets by status
     *
     * Multiple status values can be provided with comma separated strings
     *
     */
    @GET
    @Path("/findByStatus")
    @Produces({ "application/xml", "application/json" })
    Uni<List<Pet>> findPetsByStatus(@QueryParam("status") List<String> status) throws ApiException, ProcessingException;

    /**
     * Finds Pets by tags
     *
     * Multiple tags can be provided with comma separated strings. Use tag1, tag2, tag3 for testing.
     *
     * @deprecated
     */
    @Deprecated
    @GET
    @Path("/findByTags")
    @Produces({ "application/xml", "application/json" })
    Uni<List<Pet>> findPetsByTags(@QueryParam("tags") List<String> tags) throws ApiException, ProcessingException;

    /**
     * Find pet by ID
     *
     * Returns a single pet
     *
     */
    @GET
    @Path("/{petId}")
    @Produces({ "application/xml", "application/json" })
    Uni<Pet> getPetById(@PathParam("petId") Long petId) throws ApiException, ProcessingException;

    /**
     * Update an existing pet
     *
     * 
     *
     */
    @PUT
    
    @Consumes({ "application/json", "application/xml" })
    @Produces({ "application/xml", "application/json" })
    Uni<Pet> updatePet(Pet pet) throws ApiException, ProcessingException;

    /**
     * Updates a pet in the store with form data
     *
     * 
     *
     */
    @POST
    @Path("/{petId}")
    @Consumes({ "application/x-www-form-urlencoded" })
    Uni<Void> updatePetWithForm(@PathParam("petId") Long petId, @Multipart(value = "name", required = false)  String name, @Multipart(value = "status", required = false)  String status) throws ApiException, ProcessingException;

    /**
     * uploads an image
     *
     * 
     *
     */
    @POST
    @Path("/{petId}/uploadImage")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    Uni<ModelApiResponse> uploadFile(@PathParam("petId") Long petId, @Multipart(value = "additionalMetadata", required = false)  String additionalMetadata,  @Multipart(value = "file" , required = false) Attachment _fileDetail) throws ApiException, ProcessingException;
}
