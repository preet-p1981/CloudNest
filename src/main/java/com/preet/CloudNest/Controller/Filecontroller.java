package com.preet.CloudNest.Controller;

import com.preet.CloudNest.Documents.Filemetadatadocument;
import com.preet.CloudNest.Documents.usercredits;
import com.preet.CloudNest.Dto.FilemetadataDto;
import com.preet.CloudNest.Repository.Filemetadatarepository;
import com.preet.CloudNest.Service.Filemetadataservice;
import com.preet.CloudNest.Service.usercreditsservice;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class Filecontroller {

    private final Filemetadataservice filemetadataservice;
    private final usercreditsservice usercreditsservice;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestPart("files") MultipartFile[] files) throws IOException {

        List<FilemetadataDto> list = filemetadataservice.uploadfiles(files);

        usercredits currentCredits = usercreditsservice.getusercredits();

        Map<String, Object> response = new HashMap<>();
        response.put("files", list);
        response.put("remainingCredits", currentCredits.getCredits());

        return ResponseEntity.ok(response);
    }
    @GetMapping("/my")
    public ResponseEntity<?> getFiliesforcurruser(){
        List<FilemetadataDto> files = filemetadataservice.getfiles();
        return ResponseEntity.ok(files);

    }
    @GetMapping("/public/{id}")
    public ResponseEntity<?> getpublicfile(@PathVariable String id){
        FilemetadataDto file = filemetadataservice.getpublicifle(id);
        return ResponseEntity.ok(file);

    }
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable String id) throws MalformedURLException {
        FilemetadataDto downloadblefile = filemetadataservice.getdownloadablefile(id);
        Path path = Paths.get(downloadblefile.getFileLocation());
        Resource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""+downloadblefile.getName()+"" + "\"")
                .body(resource);

    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletefile(@PathVariable String id){
        filemetadataservice.deltefile(id);
        return ResponseEntity.ok("File deleted successfully");
    }

    @PatchMapping("/{id}/toggle-public")
    public ResponseEntity<?> togglepublic(@PathVariable String id){
        FilemetadataDto file = filemetadataservice.togglepublic(id);
        return ResponseEntity.ok(file);

    }

}
