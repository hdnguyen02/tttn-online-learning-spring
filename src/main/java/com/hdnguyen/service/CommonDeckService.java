package com.hdnguyen.service;

import com.hdnguyen.dao.CommonDeckDao;
import com.hdnguyen.dao.GroupDao;
import com.hdnguyen.dto.deck.CommonDeckRequest;
import com.hdnguyen.dto.deck.CommonDeckResponse;
import com.hdnguyen.dto.deck.DetailCommonDeckResponse;
import com.hdnguyen.entity.CommonDeck;
import com.hdnguyen.entity.Group;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;


@Service
@RequiredArgsConstructor
public class CommonDeckService {

    private final CommonDeckDao commonDeckDao;
    private final GroupDao groupDao;

    // tạo, hiệu chỉnh => chỉ có bên giáo viên có => yêu cầu role giáo viên.

    public boolean createCommonDeck(CommonDeckRequest commonDeckRequest) {
        Group group = groupDao.findById(commonDeckRequest.getIdGroup()).orElseThrow();

        CommonDeck commonDeck = CommonDeck.builder()

                .name(commonDeckRequest.getName())
                .description(commonDeckRequest.getDescription())
                .createAt(new Date())
                .quantityClone(0)
                .group(group)
                .build();
        try {
            commonDeckDao.save(commonDeck);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public List<CommonDeckResponse> getCommonDecksByIdGroup(long idGroup) {
        return commonDeckDao.getCommonDeckByIdGroup(idGroup).stream()
                .map(CommonDeckResponse::new).toList();
    }

    public DetailCommonDeckResponse getCommonDeck(Long idCommonDeck) {
        return new DetailCommonDeckResponse(commonDeckDao.findById(idCommonDeck).orElseThrow());
    }

    // viết hàm tạo bộ thẻ => gọi lên hàm cardService      EC

    // viết hàm hiệu chỉnh Common Deck
    public boolean editCommonDeck(Long idCommonDeck, CommonDeckRequest commonDeckRequest) {

        CommonDeck commonDeck = commonDeckDao.findById(idCommonDeck).orElseThrow();
        commonDeck.setName(commonDeckRequest.getName());
        commonDeck.setDescription(commonDeckRequest.getDescription());
        try {
            commonDeckDao.save(commonDeck);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    // viết hàm xóa học phần
    public boolean deleteCommonDeck(Long idCommonDeck) {
        try {
            commonDeckDao.deleteById(idCommonDeck);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }





}