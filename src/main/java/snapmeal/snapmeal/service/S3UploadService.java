package snapmeal.snapmeal.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import snapmeal.snapmeal.config.S3Configure;
import snapmeal.snapmeal.domain.Images;
import snapmeal.snapmeal.domain.User;
import snapmeal.snapmeal.global.code.ErrorCode;
import snapmeal.snapmeal.global.handler.GeneralException;
import snapmeal.snapmeal.global.util.AuthService;
import snapmeal.snapmeal.repository.ImageRepository;
import snapmeal.snapmeal.web.dto.DetectionDto;
import snapmeal.snapmeal.web.dto.PredictionResponseDto;
@Slf4j
@Service
@RequiredArgsConstructor
public class S3UploadService {

    private final AmazonS3 amazonS3;
    private final S3Configure s3Configure;
    private final ImageRepository imagesRepository;
    private final FastApiProxyService fastApiProxyService;
    private final AuthService authService;


    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 이미지 최대 사이즈 2MB
    @Transactional
    public PredictionResponseDto uploadPredictAndSave(MultipartFile file) {
        log.info("📌 [START] uploadPredictAndSave() 호출됨. 파일명={}, 크기={} bytes",
                file.getOriginalFilename(), file.getSize());

        try {
            // 1) 파일 크기 체크
            if (file.getSize() > MAX_FILE_SIZE) {
                log.warn("⚠️ 파일 크기 초과: {} bytes", file.getSize());
                throw new GeneralException(ErrorCode.INVALID_INPUT_VALUE,
                        "이미지 용량이 2MB를 초과했습니다. 최대 100MB까지 업로드 가능합니다.");
            }

            // 2) 로그인 사용자 조회
            User user = authService.getCurrentUser();
            log.info("👤 로그인 유저 조회 완료. userId={}", user.getUserId());

            // 3) 파일 이름 생성
            String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
            String key = UUID.randomUUID() + "-" + file.getOriginalFilename();
            log.info("📝 생성된 S3 파일명(key)={}", key);

            // 4) 메타데이터 생성
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            // 5) S3 업로드
            String bucket = s3Configure.getBucket();
            amazonS3.putObject(bucket, key, file.getInputStream(), metadata);
            log.info("📤 S3 업로드 완료. bucket={}, key={}", bucket, key);

            // 6) 업로드된 파일 URL 생성
            String fileUrl = amazonS3.getUrl(bucket, key).toString();
            log.info("🌐 업로드된 파일 URL={}", fileUrl);

            // 7) FastAPI 서버 호출
            log.info("🚀 FastAPI 서버로 이미지 URL 예측 요청 시작");
            PredictionResponseDto predictionResponse = fastApiProxyService.sendImageUrlToFastApi(fileUrl);
            log.info("✅ FastAPI 예측 완료. 응답={}", predictionResponse);

            // 8) detection 정보 추출
            List<DetectionDto> detections = predictionResponse.getDetections();
            log.info("🔍 detection 개수={}", (detections != null ? detections.size() : 0));

            int classId = -1;
            String className = "Unknown";

            if (detections != null && !detections.isEmpty()) {
                DetectionDto top = detections.get(0);
                classId = top.getClassId();
                className = top.getClassName();
                log.info("🎯 대표 클래스 선택됨: classId={}, className={}", classId, className);
            } else {
                log.info("❓ detection 비어 있음 → Unknown으로 저장");
            }

            // 9) DB 저장
            Images image = Images.builder()
                    .fileName(key)
                    .imageUrl(fileUrl)
                    .user(user)
                    .classId(classId)
                    .className(className)
                    .build();

            Images saved = imagesRepository.save(image);
            log.info("💾 DB 저장 완료. 저장된 이미지 ID={}", saved.getImgId());

            // 10) Response Setting
            predictionResponse.setImageId(Collections.singletonList(saved.getImgId()));
            predictionResponse.setDetections(detections);

            log.info("📦 응답 생성 완료. 반환 준비.");

            return predictionResponse;

        } catch (GeneralException e) {
            log.error("❗ GeneralException 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("🔥 예기치 못한 오류 발생", e);
            throw new GeneralException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "이미지 업로드 또는 예측 중 오류가 발생했습니다.");
        }
    }

    @Transactional
    public void deleteAllImagesByUser(User user) {
        List<Images> imagesList = imagesRepository.findAllByUser(user);

        imagesList.forEach(this::deleteImageFromS3);

        deleteImagesFromDatabase(user);
    }

    private void deleteImageFromS3(Images image) {
        String bucket = s3Configure.getBucket();

        // 신규 데이터: fileName 존재
        String key = image.getFileName();

        // 기존 데이터: fileName 없음 → URL에서 key 추출
        if (key == null || key.isBlank()) {
            String imageUrl = image.getImageUrl();
            if (imageUrl == null || imageUrl.isBlank()) {
                return;
            }

            key = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        }

        // key 최종 확인
        if (!key.isBlank()) {
            if (amazonS3.doesObjectExist(bucket, key)) {
                amazonS3.deleteObject(bucket, key);
            }
        }
    }

    private void deleteImagesFromDatabase(User user) {
        imagesRepository.deleteAllByUser(user);
    }


}