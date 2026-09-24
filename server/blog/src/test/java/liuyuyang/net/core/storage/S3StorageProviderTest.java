package liuyuyang.net.core.storage;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import liuyuyang.net.core.execption.CustomException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class S3StorageProviderTest {
    @Test
    void verifyCreatesReadsListsAndDeletesTemporaryObject() {
        AmazonS3 client = mock(AmazonS3.class);
        when(client.doesBucketExistV2("images")).thenReturn(true);
        when(client.listObjectsV2(any(ListObjectsV2Request.class))).thenAnswer(invocation -> {
            ListObjectsV2Request request = invocation.getArgument(0);
            S3ObjectSummary object = new S3ObjectSummary();
            object.setKey(request.getPrefix());
            ListObjectsV2Result result = new ListObjectsV2Result();
            result.getObjectSummaries().add(object);
            return result;
        });

        S3StorageProvider provider = new S3StorageProvider(properties(), client);
        provider.verify();

        verify(client).putObject(any(PutObjectRequest.class));
        verify(client).getObjectMetadata(any(GetObjectMetadataRequest.class));
        verify(client).listObjectsV2(any(ListObjectsV2Request.class));
        verify(client).deleteObject(org.mockito.ArgumentMatchers.eq("images"), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void verifyClassifiesAuthorizationFailure() {
        AmazonS3 client = mock(AmazonS3.class);
        AmazonServiceException error = new AmazonServiceException("forbidden");
        error.setStatusCode(403);
        doThrow(error).when(client).doesBucketExistV2("images");

        CustomException result = assertThrows(CustomException.class,
                () -> new S3StorageProvider(properties(), client).verify());

        assertEquals("S3 凭据无效或缺少 bucket 访问权限", result.getMessage());
    }

    private static S3StorageProperties properties() {
        return new S3StorageProperties("https://s3.example.com", "ap-guangzhou", "images", "uploads",
                "access-key", "secret-key", "", false);
    }
}
