package snapmeal.snapmeal.service;

import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.amazonaws.services.s3.AmazonS3;
import org.springframework.web.multipart.MultipartFile;
import snapmeal.snapmeal.config.S3Configure;
import snapmeal.snapmeal.domain.Images;
import snapmeal.snapmeal.domain.User;
import snapmeal.snapmeal.global.util.AuthService;
import snapmeal.snapmeal.repository.ImageRepository;
import snapmeal.snapmeal.web.dto.DetectionDto;
import snapmeal.snapmeal.web.dto.PredictionResponseDto;

import java.util.*;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class S3UploadService {

    private final AmazonS3 amazonS3;
    private final S3Configure s3Configure;
    private final ImageRepository imagesRepository;
    private final FastApiProxyService fastApiProxyService;
    private final AuthService authService;
    private final StringHttpMessageConverter stringHttpMessageConverter;

    @Transactional
    public PredictionResponseDto uploadPredictAndSave(MultipartFile file) throws IOException {
        // 로그인한 사용자 가져오기
        User user = authService.getCurrentUser();

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

        // 예측 결과에서 모든 detections 리스트 꺼내기
        List<DetectionDto> detections = predictionResponse.getDetections();

        // detections가 비어있으면 Unknown 하나로 저장
        // 엔티티 저장: 대표 정보로 첫 번째 감지 객체 선택(없으면 기본값)
        int classId = -1;
        String className = "Unknown";
        if (detections != null && !detections.isEmpty()) {
            DetectionDto top = detections.get(0);
            classId = top.getClassId();
            className = top.getClassName();
        }

        Images image = Images.builder()
                .imageUrl(fileUrl)
                .user(user)
                .classId(classId)
                .className(className)
                .build();

        Images saved = imagesRepository.save(image);

        // DTO에 ID와 detections 세팅 후 리턴
        predictionResponse.setImageId(Collections.singletonList(saved.getImgId()));
        predictionResponse.setDetections(detections);

        // 예측 결과 URL 리턴
        return predictionResponse;
    }
}