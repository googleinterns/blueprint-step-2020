package com.google.sps.utility;

import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.ProjectName;
import com.google.cloud.secretmanager.v1.Replication;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretPayload;
import com.google.cloud.secretmanager.v1.SecretVersion;
import com.google.protobuf.ByteString;

public class SecretManagerUtility {

  public static void quickstart() throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "step7-2020";
    String secretId = "API_KEY";
    quickstart(projectId, secretId);
  }

  public static void quickstart(String projectId, String secretId) throws Exception {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      /*
      // Build the parent name from the project.
      ProjectName projectName = ProjectName.of(projectId);

      // Create the parent secret.
      Secret secret =
          Secret.newBuilder()
              .setReplication(
                  Replication.newBuilder()
                      .setAutomatic(Replication.Automatic.newBuilder().build())
                      .build())
              .build();

      Secret createdSecret = client.createSecret(projectName, secretId, secret);

      // Add a secret version.
      SecretPayload payload =
          SecretPayload.newBuilder().setData(ByteString.copyFromUtf8("hello world!")).build();
      SecretVersion addedVersion = client.addSecretVersion(createdSecret.getName(), payload);
      */

      // Access the secret version.
      AccessSecretVersionResponse response = client.accessSecretVersion(secretId);

      // Print the secret payload.
      //
      // WARNING: Do not print the secret in a production environment - this
      // snippet is showing how to access the secret material.
      String data = response.getPayload().getData().toStringUtf8();
      System.out.printf("Plaintext: %s\n", data);
    }
  }
}
