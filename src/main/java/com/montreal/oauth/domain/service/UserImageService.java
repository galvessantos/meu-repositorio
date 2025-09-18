package com.montreal.oauth.domain.service;

import com.montreal.core.domain.exception.NegocioException;
import com.montreal.oauth.domain.entity.UserImage;
import com.montreal.oauth.domain.repository.UserImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserImageService {

    private final UserImageRepository userImageRepository;

    public void uploadUserImage(Long userId, MultipartFile file)  {

        try {

            UserImage userImage = new UserImage();
            userImage.setImageData(file.getBytes());
            userImage.setFileType(file.getContentType());
            userImage.setFileName(file.getOriginalFilename());
            userImage.setIdUser(userId);

            getUserImage(userId).ifPresent(userImageRepository::delete);

            userImageRepository.save(userImage);

        } catch (Exception e) {
            log.error("Erro ao salvar imagem do usuário", e);
            throw new NegocioException("Erro ao salvar imagem do usuário");
        }

    }

    public Optional<UserImage> getUserImage(Long userId) {
        return userImageRepository.findByIdUser(userId);
    }

    public void deleteUserImage(Long userId) {
        getUserImage(userId).ifPresent(userImageRepository::delete);
    }

}
