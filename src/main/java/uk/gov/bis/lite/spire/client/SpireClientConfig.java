package uk.gov.bis.lite.spire.client;

public class SpireClientConfig {

  private final String username;
  private final String password;
  private final String url;

  public SpireClientConfig(String username, String password, String url) {
    this.username = username;
    this.password = password;
    this.url = url;
  }

  String getUsername() {
    return username;
  }

  String getPassword() {
    return password;
  }

  String getUrl() {
    return url;
  }
}
