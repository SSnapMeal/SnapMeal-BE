package snapmeal.snapmeal.service;

import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.amazonaws.services.s3.AmazonS3;
import org.springframework.web.multipart.MultipartFile;
import snapmeal.snapmeal.config.S3Configure;
import snapmeal.snapmeal.domain.Images;
import snapmeal.snapmeal.domain.User;
import snapmeal.snapmeal.repository.ImageRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import snapmeal.snapmeal.repository.UserRepository;
import snapmeal.snapmeal.web.dto.PredictionResponseDto;

import java.util.HashMap;
import java.util.Map;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3UploadService {

    private final AmazonS3 amazonS3;
    private final S3Configure s3Configure;
    private final ImageRepository imagesRepository;
    private final FastApiProxyService fastApiProxyService;

    @Transactional
    public PredictionResponseDto uploadPredictAndSave(MultipartFile file, User user) throws IOException {
        // 파일 이름 생성 (UUID-원본파일명)
        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();

        // 메타데이터 생성
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        // S3에 업로드
        String bucket = s3Configure.getBucket();
        amazonS3.putObject(bucket, fileName, file.getInputStream(), metadata);

        // 업로드된 파일 URL 얻기
        String fileUrl = amazonS3.getUrl(bucket, fileName).toString();

        // 예측 서버에 요청
        PredictionResponseDto predictionResponse = fastApiProxyService.sendImageUrlToFastApi(fileUrl);

        // 예측 성공 시 DB에 저장
        Images image = Images.builder()
                .imageUrl(fileUrl)
                .user(user)   // 로그인한 사용자 정보 저장
                .classId(predictionResponse.getClassId())
                .build();

        imagesRepository.save(image);

        // 예측 결과 URL 리턴
        return predictionResponse;
    }
}
