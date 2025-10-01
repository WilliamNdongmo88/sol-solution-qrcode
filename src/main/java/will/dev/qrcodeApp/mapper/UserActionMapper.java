package will.dev.qrcodeApp.mapper;

import org.springframework.stereotype.Component;
import will.dev.qrcodeApp.dto.UserActionDto;
import will.dev.qrcodeApp.entity.UserAction;

@Component
public class UserActionMapper {

    public UserActionDto userActionToUserActionDto(UserAction userAction) {
        if (userAction == null) {
            return null;
        }

        UserActionDto dto = new UserActionDto();
        dto.setId(userAction.getId());
        dto.setTypeAction(userAction.getTypeAction());
        dto.setDescription(userAction.getDescription());
        dto.setDateAction(userAction.getDateAction());

        // Relation utilisateur
        if (userAction.getUtilisateur() != null) {
            dto.setUserId(userAction.getUtilisateur().getId());
            dto.setUserName(userAction.getUtilisateur().getNom());
        }

        // Relation qrCodeMetadata (si tu as besoin de l’ajouter)
//        if (userAction.getQrCodeMetadata() != null) {
//            dto.setQrCodeId(userAction.getQrCodeMetadata().getId());
//        }

        return dto;
    }

    public UserAction userActionDtoToUserAction(UserActionDto dto) {
        if (dto == null) {
            return null;
        }

        UserAction entity = new UserAction();
        entity.setId(dto.getId());
        entity.setTypeAction(dto.getTypeAction());
        entity.setDescription(dto.getDescription());
        entity.setDateAction(dto.getDateAction());

        // ⚠ utilisateur et qrCodeMetadata sont ignorés
        // => ils devront être associés dans ton service avant save()

        return entity;
    }
}
