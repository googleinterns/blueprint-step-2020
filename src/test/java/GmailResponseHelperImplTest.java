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

import com.google.sps.model.GmailResponseHelper;
import com.google.sps.model.GmailResponseHelperImpl;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the GmailResponseHelperImpl and ensures that statistics for the GmailResponse are
 * calculated correctly
 */
public class GmailResponseHelperImplTest extends GmailTestBase {
  private static final GmailResponseHelper gmailResponseHelper = new GmailResponseHelperImpl();

  @Test
  public void emptyListUnreadEmailsMHours() {
    int unreadEmailsMHours =
        gmailResponseHelper.countEmailsFromMHours(NO_MESSAGES, DEFAULT_M_HOURS);
    Assert.assertEquals(0, unreadEmailsMHours);
  }

  @Test
  public void emptyListUnreadImportantEmailsFromNDays() {
    int unreadImportantEmails = gmailResponseHelper.countImportantEmails(NO_MESSAGES);
    Assert.assertEquals(0, unreadImportantEmails);
  }

  @Test
  public void emptyListFindMostFrequentSender() {
    Optional<String> sender = gmailResponseHelper.findMostFrequentSender(NO_MESSAGES);
    Assert.assertFalse(sender.isPresent());
  }

  @Test
  public void calculateUnreadEmailsMHours() {
    int unreadEmailsMHours =
        gmailResponseHelper.countEmailsFromMHours(
            SOME_MESSAGES_HALF_WITHIN_M_HOURS, DEFAULT_M_HOURS);
    Assert.assertEquals(SOME_MESSAGES_HALF_WITHIN_M_HOURS.size(), unreadEmailsMHours * 2);
  }

  @Test
  public void calculateUnreadImportantEmailsFromNDays() {
    int numberOfImportantEmails =
        gmailResponseHelper.countImportantEmails(SOME_IMPORTANT_MESSAGES_WITH_ONE_UNIMPORTANT);
    Assert.assertEquals(
        SOME_IMPORTANT_MESSAGES_WITH_ONE_UNIMPORTANT.size() - 1, numberOfImportantEmails);
  }

  @Test
  public void getMostFrequentSenderWhenContactNameIsPresent() {
    String senderName =
        gmailResponseHelper
            .findMostFrequentSender(MESSAGES_MAJORITY_SENDER_ONE_WITH_CONTACT_NAME)
            .get();
    Assert.assertEquals(SENDER_ONE_NAME, senderName);
  }

  @Test
  public void getMostFrequentSenderWhenOnlyEmailIsPresent() {
    String senderName =
        gmailResponseHelper
            .findMostFrequentSender(MESSAGES_MAJORITY_SENDER_ONE_WITHOUT_CONTACT_NAME)
            .get();
    Assert.assertEquals(SENDER_ONE_EMAIL, senderName);
  }

  @Test
  public void checkSenderEqualFrequenciesPreferMostRecentSender() {
    // This is the case when there is an equal amount of sent messages from two senders
    // (i.e. 2 from sender A, 2 from sender B).
    // The sender who sent the latest/most recent contact should be returned.
    // NOTE: in the case that the frequencies are tied, and they both sent emails at the exact same
    // time
    // (which is absurdly rare), the system can return either of the senders (it won't matter to the
    // user).
    // Thus, that circumstance will not be tested.

    String senderName =
        gmailResponseHelper
            .findMostFrequentSender(MESSAGES_SPLIT_SENDERS_SENDER_ONE_WITH_CONTACT_NAME_MOST_RECENT)
            .get();
    Assert.assertEquals(SENDER_ONE_NAME, senderName);
  }
}
