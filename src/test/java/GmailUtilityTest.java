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

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.common.collect.ImmutableList;
import com.google.sps.exceptions.GmailMessageFormatException;
import com.google.sps.utility.GmailUtility;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Test GmailClient static functions */
@RunWith(JUnit4.class)
public final class GmailUtilityTest {
  private static final String HEADER_NAME = "SampleName";
  private static final String HEADER_VALUE = "SampleName";
  private static final MessagePartHeader headerWithHeaderValue =
      new MessagePartHeader().setName(HEADER_NAME).setValue(HEADER_VALUE);
  private static final List<MessagePartHeader> HEADERS_WITH_HEADER_NAME =
      ImmutableList.of(
          headerWithHeaderValue,
          new MessagePartHeader().setName("junk_header_one").setValue("junk_value_one"),
          new MessagePartHeader().setName("junk_header_two").setValue("junk_value_two"));
  private static final List<MessagePartHeader> HEADERS_WITHOUT_HEADER_NAME =
      ImmutableList.of(
          new MessagePartHeader().setName("junk_header_one").setValue("junk_value_one"),
          new MessagePartHeader().setName("junk_header_two").setValue("junk_value_two"));
  private static final Message messageNoPayload = new Message();
  private static final Message messageNoHeaders = new Message().setPayload(new MessagePart());
  private static final Message messageEmptyHeaders =
      new Message().setPayload(new MessagePart().setHeaders(Collections.emptyList()));
  private static final Message messageWithHeaderName =
      new Message().setPayload(new MessagePart().setHeaders(HEADERS_WITH_HEADER_NAME));
  private static final Message messageWithoutHeaderName =
      new Message().setPayload(new MessagePart().setHeaders(HEADERS_WITHOUT_HEADER_NAME));

  @Test(expected = GmailMessageFormatException.class)
  public void extractHeaderFromMessageWithNoPayload() {
    GmailUtility.extractHeader(messageNoPayload, HEADER_NAME);
  }

  @Test(expected = GmailMessageFormatException.class)
  public void extractHeaderFromMessageWithNoHeaders() {
    GmailUtility.extractHeader(messageNoHeaders, HEADER_NAME);
  }

  @Test(expected = GmailMessageFormatException.class)
  public void extractHeaderFromMessageWithEmptyHeaders() {
    GmailUtility.extractHeader(messageEmptyHeaders, HEADER_NAME);
  }

  @Test(expected = GmailMessageFormatException.class)
  public void extractHeaderFromMessageWithoutDesiredHeader() {
    GmailUtility.extractHeader(messageWithoutHeaderName, HEADER_NAME);
  }

  @Test()
  public void extractHeaderFromMessageWithDesiredHeader() {
    MessagePartHeader actualHeader = GmailUtility.extractHeader(messageWithHeaderName, HEADER_NAME);

    Assert.assertEquals(headerWithHeaderValue, actualHeader);
  }
}
