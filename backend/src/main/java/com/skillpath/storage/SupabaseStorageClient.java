package com.skillpath.storage;

import com.skillpath.exception.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
public class SupabaseStorageClient {

    private final RestTemplate restTemplate;

    @Value("${supabase.url}")
    private String supabaseUrl;
    @Value("${supabase.service-role-key}")
    private String serviceRoleKey;
    @Value("${supabase.avatar-bucket}")
    private String avatarBucket;

    public SupabaseStorageClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Uploads bytes to {avatarBucket}/{path}, overwriting whatever was at
     * that path before (via the x-upsert header, so re-uploading a new
     * photo never leaves the old one orphaned), and returns the public
     * URL. Requires avatarBucket to be a PUBLIC bucket in Supabase — if
     * it's private instead, this needs to switch to Supabase's
     * signed-URL flow.
     */
    public String uploadAvatar(String path, byte[] bytes, String contentType) {
        if (supabaseUrl == null || supabaseUrl.isBlank()
                || serviceRoleKey == null || serviceRoleKey.isBlank()) {
            throw new StorageException("Image storage isn't configured yet - no Supabase URL/key is set.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType != null ? contentType : "image/jpeg"));
        headers.set("Authorization", "Bearer " + serviceRoleKey);
        headers.set("apikey", serviceRoleKey);
        headers.set("x-upsert", "true");
        HttpEntity<byte[]> request = new HttpEntity<>(bytes, headers);

        String uploadUrl = supabaseUrl + "/storage/v1/object/" + avatarBucket + "/" + path;
        try {
            restTemplate.exchange(uploadUrl, HttpMethod.POST, request, String.class);
        } catch (HttpStatusCodeException e) {
            throw new StorageException("Couldn't upload your photo. Please try again.", e);
        } catch (ResourceAccessException e) {
            throw new StorageException(
                "Couldn't reach image storage - please check your connection and try again.", e);
        } catch (Exception e) {
            throw new StorageException("Something went wrong uploading your photo.", e);
        }

        return supabaseUrl + "/storage/v1/object/public/" + avatarBucket + "/" + path
                + "?t=" + System.currentTimeMillis();
    }
}
