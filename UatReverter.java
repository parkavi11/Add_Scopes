package org.wso2.patchvalidator.revertor;

import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.wso2.patchvalidator.client.UatClient;
import org.wso2.patchvalidator.constants.Constants;
import org.wso2.patchvalidator.exceptions.ServiceException;
import org.wso2.patchvalidator.util.PropertyLoader;

/**
 * Revert WUM UAT database when reverting patch.
 * Delete WUM staging rows related to patch
 * Delete WUM UAT rows related to patch
 */
class UatReverter {

    private static Properties prop = PropertyLoader.getInstance().prop;

    boolean revertUat(String patchId) {

        boolean isWumDevReverted;
        boolean isWumStgReverted;
        boolean isWumUatReverted;

//        try {
//            isWumDevReverted = deleteWumDev(patchId);
//        } catch (ServiceException ex) {
//            throw new ServiceException("Exception occurred when reverting WUM DEV, patchId:" + patchId,
//                    ex.getDeveloperMessage(), ex);
//        }
//        return isWumDevReverted;

        try {
            isWumStgReverted = deleteWumUat(patchId);
        } catch (ServiceException ex) {
            throw new ServiceException("Exception occurred when reverting WUM Stg, patchId:" + patchId,
                    ex.getDeveloperMessage(), ex);
        }

        if (isWumStgReverted) {
            try {
                isWumUatReverted = deleteWumUat(patchId);

                if (isWumUatReverted) {
                    return true;
                } else {
                    throw new ServiceException("WUM Stg revert successful. WUM UAT revert failed. patchId:" + patchId,
                            "WUM Staging revert successful, WUM UAT revert failed for the update \"" +
                                    patchId + "\", Please contact admin.");
                }
            } catch (ServiceException ex) {
                throw new ServiceException("WUM Stg reverted successfully. Exception occurred when reverting" +
                        " WUM UAT, patchId:" + patchId, ex.getDeveloperMessage(), ex);
            }
        } else {
            throw new ServiceException("WUM Stg revert failed, patchId:" + patchId,
                    "WUM Staging revert failed for the update \"" + patchId + "\", Please contact admin.");
        }
    }

    private static boolean deleteWumStg(String updateId) {

        String uri = prop.getProperty("wumStgDeleteUrl");
        String jwtAssertionValue = getJwtAssertionValue(prop.getProperty("wumJwtAssertValue"));
        String forwardedForValue = prop.getProperty("forwardedForValue");

        JSONObject AccessTokenObj;
        boolean isUatUpdateDeleted;

        try {
            AccessTokenObj = UatClient.getUatAccessToken(prop.getProperty(Constants.WUM_UAT_GRANT_TYPE),
                    prop.getProperty(Constants.WUM_UAT_USERNAME), prop.getProperty(Constants.WUM_UAT_PASSWORD),
                    prop.getProperty(Constants.WUM_UAT_SCOPE), prop.getProperty(Constants.WUM_UAT_APP_KEY), prop.getProperty(Constants.WUM_UAT_ACCESS_TOKEN_URI));
        } catch (ServiceException ex) {
            throw new ServiceException("Exception occurred, when retrieving access token from WUM Stg. " +
                    " wumStgAccessTokenUri:" + prop.getProperty("wumStgAccessTokenUri") +
                    " wumStgGrantType:" + prop.getProperty("wumStgGrantType") +
                    " wumStgGrantTypeValue:" + prop.getProperty("wumStgGrantTypeValue") +
                    " wumStgAccTokenAuthorization:" + prop.getProperty("wumStgAccTokenAuthorization") +
                    " wumStgAppKey:" + prop.getProperty("wumStgAppKey") +
                    " wumStgUsername:" + prop.getProperty("wumStgUsername") +
                    " wumStgScope:" + prop.getProperty("wumStgScope"),
                    ex.getDeveloperMessage(), ex);
        }
        String authorizationValue = AccessTokenObj.get("token_type") + " " + AccessTokenObj.get("access_token");

        try {
            isUatUpdateDeleted = UatClient.deleteUatUpdate(updateId, uri, jwtAssertionValue, forwardedForValue,
                    authorizationValue);
            if (isUatUpdateDeleted) {
                return true;
            } else {
                throw new ServiceException("Deleting WUM Stg update failed, " + " updateId:" + updateId +
                        " uri:" + uri + " jwtAssertionValue:" + jwtAssertionValue + " forwardedForValue:" +
                        forwardedForValue + " authorizationValue:" + authorizationValue,
                        "Deleting update failed for the update \"" + updateId + "\" in WUM staging, " +
                                "Please contact admin.");
            }
        } catch (ServiceException ex) {
            throw new ServiceException("Exception occurred when deleting WUM Stg update, " + " updateId:" + updateId +
                    " uri:" + uri + " jwtAssertionValue:" + jwtAssertionValue + " forwardedForValue:" +
                    forwardedForValue + " authorizationValue:" + authorizationValue, ex.getDeveloperMessage(), ex);
        }
    }


    private static boolean deleteWumUat(String updateId) {

        String uri = prop.getProperty(Constants.WUM_UAT_DELETE_URL);
        String jwtAssertionValue = getJwtAssertionValue(prop.getProperty("wumJwtAssertValue"));
        String forwardedForValue = prop.getProperty("forwardedForValue");

        JSONObject AccessTokenObj;
        boolean isUatUpdateDeleted;

        try {
            AccessTokenObj = UatClient.getUatAccessToken(prop.getProperty(Constants.WUM_UAT_GRANT_TYPE),
                    prop.getProperty(Constants.WUM_UAT_USERNAME), prop.getProperty(Constants.WUM_UAT_PASSWORD),
                    prop.getProperty(Constants.WUM_UAT_SCOPE), prop.getProperty(Constants.WUM_UAT_APP_KEY), prop.getProperty(Constants.WUM_UAT_ACCESS_TOKEN_URI));
        } catch (ServiceException ex) {
            throw new ServiceException("Exception occurred, when retrieving access token from WUM UAT. " +
                    " wumUatAccessTokenUri:" + prop.getProperty(Constants.WUM_UAT_ACCESS_TOKEN_URI) +
                    " wumUatGrantType:" + prop.getProperty(Constants.WUM_UAT_GRANT_TYPE) +
                    " wumUatGrantTypeValue:" + prop.getProperty(Constants.WUM_UAT_GRANT_TYPE_VALUE) +
                    " wumUatAccTokenAuthorization:" + prop.getProperty(Constants.WUM_UAT_ACCESS_TOKEN_AUTHORIZATION) +
                    " wumUatAppKey:" + prop.getProperty(Constants.WUM_UAT_APP_KEY) +
                    " wumUatUsername:" + prop.getProperty(Constants.WUM_UAT_USERNAME) +
                    " wumUatScope:" + prop.getProperty(Constants.WUM_UAT_SCOPE) +
                    " wumUatPassword:" + prop.getProperty(Constants.WUM_UAT_PASSWORD),
                    ex.getDeveloperMessage(), ex);
        }
        String authorizationValue = AccessTokenObj.get("token_type") + " " + AccessTokenObj.get("access_token");

        try {
            isUatUpdateDeleted = UatClient.deleteUatUpdate(updateId, uri, jwtAssertionValue, forwardedForValue,
                    authorizationValue);
            if (isUatUpdateDeleted) {
                return true;
            } else {
                throw new ServiceException("Deleting WUM UAT update failed, " + " updateId:" + updateId +
                        " uri:" + uri + " jwtAssertionValue:" + jwtAssertionValue + " forwardedForValue:" +
                        forwardedForValue + " authorizationValue:" + authorizationValue,
                        "Deleting update failed for the update \"" + updateId + "\" in WUM UAT, " +
                                "Please contact admin.");
            }
        } catch (ServiceException ex) {
            throw new ServiceException("Exception occurred when deleting WUM UAT update, " + " updateId:" + updateId +
                    " uri:" + uri + " jwtAssertionValue:" + jwtAssertionValue + " forwardedForValue:" +
                    forwardedForValue + " authorizationValue:" + authorizationValue, ex.getDeveloperMessage(), ex);
        }
    }


    private static String getJwtAssertionValue(String username) {

        String usernameObjStr = "{\"http://wso2.org/claims/emailaddress\":\"" + username + "\"}";
        String jwtAssertStr = usernameObjStr + "." + usernameObjStr + "." + usernameObjStr;
        byte[] bytesEncoded = Base64.encodeBase64(jwtAssertStr.getBytes());
        return new String(bytesEncoded);
    }
}
