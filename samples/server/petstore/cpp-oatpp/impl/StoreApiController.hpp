/**
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

/*
* StoreApiController.hpp
*
* 
*/

#ifndef STORE_API_IMPL_H_
#define STORE_API_IMPL_H_


#include <StoreApi.hpp>
#include "oatpp/core/Types.hpp"


#include "Order.hpp"

namespace org::openapitools::server::api
{

class  StoreApiController : public StoreApi {
public:
    /// <summary>
    /// Delete purchase order by ID
    /// </summary>
    /// <remarks>
    /// For valid response try integer IDs with value &lt; 1000. Anything above 1000 or nonintegers will generate API errors
    /// </remarks>
    /// <param name="request">HTTP Request</param>
    /// <param name="orderId">ID of the order that needs to be deleted</param>
    virtual std::shared_ptr<oatpp::web::protocol::http::outgoing::Response> delete_order(const std::shared_ptr<IncomingRequest> &request, const oatpp::String &orderId);
    /// <summary>
    /// Returns pet inventories by status
    /// </summary>
    /// <remarks>
    /// Returns a map of status codes to quantities
    /// </remarks>
    /// <param name="request">HTTP Request</param>
    virtual std::shared_ptr<oatpp::web::protocol::http::outgoing::Response> get_inventory(const std::shared_ptr<IncomingRequest> &request);
    /// <summary>
    /// Find purchase order by ID
    /// </summary>
    /// <remarks>
    /// For valid response try integer IDs with value &lt;&#x3D; 5 or &gt; 10. Other values will generate exceptions
    /// </remarks>
    /// <param name="request">HTTP Request</param>
    /// <param name="orderId">ID of pet that needs to be fetched</param>
    virtual std::shared_ptr<oatpp::web::protocol::http::outgoing::Response> get_order_by_id(const std::shared_ptr<IncomingRequest> &request, const oatpp::Int64 &orderId);
    /// <summary>
    /// Place an order for a pet
    /// </summary>
    /// <remarks>
    /// 
    /// </remarks>
    /// <param name="request">HTTP Request</param>
    /// <param name="order">order placed for purchasing the pet</param>
    virtual std::shared_ptr<oatpp::web::protocol::http::outgoing::Response> place_order(const std::shared_ptr<IncomingRequest> &request, const oatpp::Object<org::openapitools::server::model::Order> &order);
};

} // namespace org::openapitools::server::api



#endif
