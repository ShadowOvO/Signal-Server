/*
 * Copyright (C) 2013 Open WhisperSystems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.whispersystems.textsecuregcm.s3;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;

import java.net.URL;
import java.util.Date;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.xmlpull.v1.XmlPullParserException;

import io.minio.MinioClient;
import io.minio.errors.MinioException;

public class UrlSigner {

  private static final long   DURATION = 60 * 60 * 1000;

  private final AWSCredentials credentials;
  private final String bucket;

  public UrlSigner(String accessKey, String accessSecret, String bucket) {
    this.credentials = new BasicAWSCredentials(accessKey, accessSecret);
    this.bucket      = bucket;
  }

  public URL getPreSignedUrl(long attachmentId, HttpMethod method, boolean unaccelerated) {
    AmazonS3                    client  = new AmazonS3Client(credentials);
    GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, String.valueOf(attachmentId), method);
    
    request.setExpiration(new Date(System.currentTimeMillis() + DURATION));
    request.setContentType("application/octet-stream");

    if (unaccelerated) {
      client.setS3ClientOptions(S3ClientOptions.builder().setPathStyleAccess(true).build());
    } else {
      client.setS3ClientOptions(S3ClientOptions.builder().setAccelerateModeEnabled(true).build());
    }

    return client.generatePresignedUrl(request);
  }


  public String getPreSignedUrl(long attachmentId, HttpMethod method) throws InvalidKeyException, NoSuchAlgorithmException, IOException, XmlPullParserException, MinioException {
    String request = geturl(bucket, String.valueOf(attachmentId), method);
    return request;
  }

  public String geturl( String bucketname, String attachmentId, HttpMethod method) throws NoSuchAlgorithmException,
          IOException, InvalidKeyException, XmlPullParserException, MinioException {

    String url = null;


    MinioClient minioClient = new MinioClient("http://114.115.142.90:9000", "Q3AM3UQ867SPQQA43P2F", "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG");

    try {
      if(method==HttpMethod.PUT){
        url = minioClient.presignedPutObject(bucketname, attachmentId, 60 * 60 * 24);
      }
      if(method==HttpMethod.GET){
        url = minioClient.presignedGetObject(bucketname, attachmentId);
      }
      System.out.println(url);
    } catch(MinioException e) {
      System.out.println("Error occurred: " + e);
    } catch (java.security.InvalidKeyException e) {
      e.printStackTrace();
    }

    return url;
  }

}
