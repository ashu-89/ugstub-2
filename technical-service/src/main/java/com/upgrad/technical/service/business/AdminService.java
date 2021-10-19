package com.upgrad.technical.service.business;


import com.upgrad.technical.service.dao.ImageDao;
import com.upgrad.technical.service.dao.UserDao;
import com.upgrad.technical.service.entity.ImageEntity;
import com.upgrad.technical.service.entity.UserAuthTokenEntity;
import com.upgrad.technical.service.exception.ImageNotFoundException;
import com.upgrad.technical.service.exception.UnauthorizedException;
import com.upgrad.technical.service.exception.UserNotSignedInException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;

@Service
public class AdminService {

    @Autowired
    private ImageDao imageDao;

    public ImageEntity getImage(final String imageUuid, final String authorization) throws ImageNotFoundException, UnauthorizedException, UserNotSignedInException {

        UserAuthTokenEntity userAuthTokenEntity = imageDao.getUserAuthToken(authorization);

        if (userAuthTokenEntity == null) {
            throw new UserNotSignedInException("USR-001", "You are not Signed in, sign in first to get the details of the image");
        }

        String role = userAuthTokenEntity.getUser().getRole();
        if (role.equals("admin")) {
            ImageEntity imageEntity = imageDao.getImage(imageUuid);
            if (imageEntity == null) {
                throw new ImageNotFoundException("IMG-001", "Image with Uuid not found");
            }
            return imageEntity;
        } else
            throw new UnauthorizedException("ATH-001", "UNAUTHORIZED Access, Entered user is not an admin");
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public ImageEntity updateImage(final ImageEntity imageEntity, final String authorization) throws ImageNotFoundException, UnauthorizedException, UserNotSignedInException {
        //Complete this method
        //Firstly check whether the access token is a valid one(exists in user_auth_tokens table). If not valid throw UserNotSignedException

        UserAuthTokenEntity userAuthToken = imageDao.getUserAuthToken(authorization);
        if(userAuthToken == null)
            throw new UserNotSignedInException("USR-001", "Please sign in to update the image");

        //Then check the role of the user with entered access token (if nonadmin then throw UnauthorizedException)

        if(userAuthToken.getUser().getRole() == "nonadmin")
            throw new UnauthorizedException("ATH-001", "You are not authorized to update image");

        //If the role is admin, get the existing image in the database with entered image id using getImageById() method in ImageDao class
        ImageEntity existingImage = new ImageEntity();
        if(userAuthToken.getUser().getRole() == "admin"){
            existingImage = imageDao.getImageById(imageEntity.getId());

            //If the image with entered image id does not exist throw ImageNotFoundException
            if(existingImage == null)
                throw new ImageNotFoundException("IMG-001", "Invalid image id entered");

            //If the image with entered image id exists in the database and is returned,
            //try to set all the attributes of the new image(received by this method) using the existing image
            existingImage.setDescription(imageEntity.getDescription());
            existingImage.setImage(imageEntity.getImage());
            existingImage.setStatus(imageEntity.getStatus());
            existingImage.setName(imageEntity.getName());
            existingImage.setId(imageEntity.getId());
            existingImage.setUuid(imageEntity.getUuid());
            existingImage.setCreated_at(imageEntity.getCreated_at());
            existingImage.setNo_of_likes(imageEntity.getNo_of_likes());
            existingImage.setUser_id(imageEntity.getUser_id());

            //Call updateImage() method for imageDao to update an image
            //Note that ImageNotFoundException , UserNotFoundException and UnauthorizedException has been implemented
            //Note that this method returns ImageEntity type object


        }

        return imageDao.updateImage(existingImage);
    }
}
