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

/** Fake version of HttpServletResponse used for testing */
public class HttpServletResponseFake {
  // Stores the status code of the http request (default to 200 until error sent)
  private int responseCode = 200;

  // Used for storing and returning the passed JSON response
  private final StringWriter stringWriter;
  private final PrintWriter printWriter;

  public HttpServletResponseFake(StringWriter stringWriter) {
    this.stringWriter = stringWriter;
    this.printWriter = new PrintWriter(stringWriter);
  }

  /**
   * Get the stringwriter used for writing the response
   *
   * @return Stringwriter object
   */
  public StringWriter getStringWriter() {
    printWriter.flush();
    return stringWriter;
  }

  public void sendError(int i, String s) {
    responseCode = i;
  }

  public void sendError(int i) {
    responseCode = i;
  }

  public void setStatus(int i) {
    responseCode = i;
  }

  public void setStatus(int i, String s) {
    responseCode = i;
  }

  public int getStatus() {
    return responseCode;
  }

  public PrintWriter getWriter() {
    return printWriter;
  }

  public void setContentType(String s) {}
}
