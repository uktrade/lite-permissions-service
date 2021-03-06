package uk.gov.bis.lite.permissions.spire.clients;

import uk.gov.bis.lite.common.spire.client.SpireClient;
import uk.gov.bis.lite.common.spire.client.SpireClientConfig;
import uk.gov.bis.lite.common.spire.client.SpireRequestConfig;
import uk.gov.bis.lite.common.spire.client.errorhandler.ErrorHandler;
import uk.gov.bis.lite.common.spire.client.parser.SpireParser;

public class SpireReferenceClient extends SpireClient<String> {

  public SpireReferenceClient(SpireParser<String> parser,
                              SpireClientConfig clientConfig,
                              SpireRequestConfig requestConfig,
                              ErrorHandler errorHandler) {
    super(parser, clientConfig, requestConfig, errorHandler);
  }
}
