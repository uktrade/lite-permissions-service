package uk.gov.bis.lite.spire;


import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

public class SpireRequest {

  private String endpointTarget;
  private SpireClient.Endpoint endpoint;

  private SOAPMessage message;
  private SOAPElement parent;

  public SpireRequest(SpireClient.Endpoint endpoint) {
    this.endpoint = endpoint;
  }

  public void setSoapMessage(SOAPMessage message) {
    this.message = message;
    try {
      this.parent = (SOAPElement) message.getSOAPPart().getEnvelope().getBody().getChildElements().next();
    } catch (SOAPException e) {
      e.printStackTrace();
    }
  }

  public void addChild(String childName, String childText) {
    try {
      SOAPElement child = parent.addChildElement(childName);
      child.addTextNode(childText);
      message.saveChanges();
    } catch (SOAPException e) {
      throw new RuntimeException("An error occurred adding child element", e);
    }
  }

  public void addChildList(String listName, String elementName, String childName, String childText) {
    try {
      SOAPElement list = parent.addChildElement(listName);
      SOAPElement element = list.addChildElement(elementName);
      SOAPElement child = element.addChildElement(childName);
      child.addTextNode(childText);
      message.saveChanges();
    } catch (SOAPException e) {
      throw new RuntimeException("An error occurred adding child element", e);
    }
  }

  public SpireClient.Endpoint getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(SpireClient.Endpoint endpoint) {
    this.endpoint = endpoint;
  }

  public SOAPMessage getSoapMessage() {
    return message;
  }

  public String getEndpointTarget() {
    return endpointTarget;
  }

  public void setEndpointTarget(String endpointTarget) {
    this.endpointTarget = endpointTarget;
  }
}
