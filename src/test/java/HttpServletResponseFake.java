// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Locale;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/** Fake version of HttpServletResponse used for testing */
public class HttpServletResponseFake implements HttpServletResponse {
  // Stores the status code of the http request (default to 200 until error sent)
  private int responseCode = 200;

  // Used for storing and returning the passed JSON response
  private StringWriter stringWriter = new StringWriter();
  private PrintWriter printWriter = new PrintWriter(stringWriter);

  public StringWriter getStringWriter() {
    return stringWriter;
  }

  @Override
  public void addCookie(Cookie cookie) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsHeader(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String encodeURL(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String encodeRedirectURL(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String encodeUrl(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String encodeRedirectUrl(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void sendError(int i, String s) {
    responseCode = i;
  }

  @Override
  public void sendError(int i) {
    responseCode = i;
  }

  @Override
  public void sendRedirect(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setDateHeader(String s, long l) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addDateHeader(String s, long l) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setHeader(String s, String s1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addHeader(String s, String s1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setIntHeader(String s, int i) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addIntHeader(String s, int i) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setStatus(int i) {
    responseCode = i;
  }

  @Override
  public void setStatus(int i, String s) {
    responseCode = i;
  }

  @Override
  public int getStatus() {
    return responseCode;
  }

  @Override
  public String getHeader(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<String> getHeaders(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<String> getHeaderNames() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getCharacterEncoding() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getContentType() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ServletOutputStream getOutputStream() {
    throw new UnsupportedOperationException();
  }

  @Override
  public PrintWriter getWriter() {
    return printWriter;
  }

  @Override
  public void setCharacterEncoding(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setContentLength(int i) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setContentLengthLong(long l) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setContentType(String s) {}

  @Override
  public void setBufferSize(int i) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getBufferSize() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void flushBuffer() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void resetBuffer() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isCommitted() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void reset() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setLocale(Locale locale) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Locale getLocale() {
    throw new UnsupportedOperationException();
  }
}
