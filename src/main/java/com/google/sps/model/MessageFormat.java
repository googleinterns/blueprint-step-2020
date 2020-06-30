package com.google.sps.model;

/**
 * Encapsulates possible values for the "format" query parameter in the Gmail GET message method
 * FULL: Returns full email message data METADATA: Returns only email message ID, labels, and email
 * headers MINIMAL: Returns only email message ID and labels; does not return the email headers,
 * body, or payload. RAW: Returns the full email message data with body content in the raw field as
 * a base64url encoded string;
 */
public enum MessageFormat {
  FULL("full"),
  METADATA("metadata"),
  MINIMAL("minimal"),
  RAW("raw");

  public final String formatValue;

  MessageFormat(String formatValue) {
    this.formatValue = formatValue;
  }
}
