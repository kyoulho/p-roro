package io.playce.roro.api.domain.common.service;

import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.ResourceNotFoundException;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.common.code.Domain101;
import io.playce.roro.common.dto.common.thirdparty.ThirdPartySolutionListResponse;
import io.playce.roro.common.dto.common.thirdparty.ThirdPartySolutionListResponse.DiscoveryType;
import io.playce.roro.common.dto.common.thirdparty.ThirdPartySolutionRequest;
import io.playce.roro.common.dto.common.thirdparty.ThirdPartySolutionRequest.ThirdPartySearchTypeRequest;
import io.playce.roro.common.dto.common.thirdparty.ThirdPartySolutionResponse;
import io.playce.roro.common.dto.common.thirdparty.ThirdPartySolutionResponse.ThirdPartySearchTypeDetail;
import io.playce.roro.common.dto.common.thirdparty.ThirdPartySolutionResponse.ThirdPartySearchTypeResponse;
import io.playce.roro.jpa.entity.ThirdPartySearchType;
import io.playce.roro.jpa.entity.ThirdPartySolution;
import io.playce.roro.jpa.entity.UserAccess;
import io.playce.roro.jpa.repository.ThirdPartySearchTypeRepository;
import io.playce.roro.jpa.repository.ThirdPartySolutionRepository;
import io.playce.roro.jpa.repository.UserAccessRepository;
import io.playce.roro.mybatis.domain.thirdparty.ThirdPartyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThirdPartyService {

    private final ThirdPartySolutionRepository thirdPartySolutionRepository;
    private final ThirdPartySearchTypeRepository thirdPartySearchTypeRepository;
    private final UserAccessRepository userAccessRepository;
    private final ThirdPartyMapper thirdPartyMapper;
    private final ThirdPartyExcelExporter thirdPartyExcelExporter;

    private final ModelMapper modelMapper;

    public List<ThirdPartySolutionListResponse> getThirdPartySolutions() {
        List<ThirdPartySolutionListResponse> thirdPartySolutionListResponse = thirdPartyMapper.selectThirdPartyList();

        for (ThirdPartySolutionListResponse tempThirdPartySolution : thirdPartySolutionListResponse) {
            List<String> tempDiscoveryTypes = Stream.of(tempThirdPartySolution.getDiscoveryType().split(",", -1))
                    .collect(Collectors.toList());

            List<DiscoveryType> discoveryTypes = new ArrayList<>();

            for (String tempDiscoveryType : tempDiscoveryTypes) {
                String[] discovery = tempDiscoveryType.split(" ");
                discoveryTypes.add(new DiscoveryType(discovery[0], discovery[1]));
            }
            tempThirdPartySolution.setDiscoveryTypes(discoveryTypes);
        }

        return thirdPartySolutionListResponse;
    }

    public ThirdPartySolutionResponse getThirdPartySolution(Long thirdPartySolutionId) {
        ThirdPartySolution thirdPartySolution = thirdPartySolutionRepository.findById(thirdPartySolutionId)
                .orElseThrow(() -> new ResourceNotFoundException("ThirdPartySolutionId(" + thirdPartySolutionId + ") does Not Found."));

        List<ThirdPartySearchType> thirdPartySearchTypes = thirdPartySearchTypeRepository.findByThirdPartySolutionId(thirdPartySolutionId);
        // 사용자 정보.
        UserAccess userAccess = userAccessRepository.findById(thirdPartySolution.getRegistUserId()).orElse(null);

        ThirdPartySolutionResponse thirdPartySolutionResponse = modelMapper.map(thirdPartySolution, ThirdPartySolutionResponse.class);

        List<ThirdPartySearchTypeResponse> thirdPartySearchTypeResponses = new ArrayList<>();

        // SearchType 기준으로 그루핑을 한다.
        Map<String, List<ThirdPartySearchType>> thirdPartySearchTypeMap = thirdPartySearchTypes.stream()
                .collect(Collectors.groupingBy(ThirdPartySearchType::getSearchType));

        for (String searchType : thirdPartySearchTypeMap.keySet()) {
            ThirdPartySearchTypeResponse thirdPartySearchTypeResponse = new ThirdPartySearchTypeResponse();
            thirdPartySearchTypeResponse.setSearchType(searchType);
            thirdPartySearchTypeResponse.setDisplayOrder(getSearchTypeOrder(searchType));

            List<ThirdPartySearchTypeDetail> thirdPartySearchTypeDetails = new ArrayList<>();

            for (ThirdPartySearchType thirdPartySearchType : thirdPartySearchTypeMap.get(searchType)) {
                thirdPartySearchTypeDetails.add(modelMapper.map(thirdPartySearchType, ThirdPartySearchTypeDetail.class));
            }

            thirdPartySearchTypeResponse.setValues(thirdPartySearchTypeDetails);
            thirdPartySearchTypeResponses.add(thirdPartySearchTypeResponse);
        }

        thirdPartySolutionResponse.setRegistUserId(userAccess.getUserLoginId());
        thirdPartySolutionResponse.setThirdPartySearchTypes(thirdPartySearchTypeResponses.stream()
                .sorted(Comparator.comparing(ThirdPartySearchTypeResponse::getDisplayOrder))
                .collect(Collectors.toList()));

        return thirdPartySolutionResponse;
    }

    @Transactional
    public Map<String, Object> createThirdPartySolution(ThirdPartySolutionRequest thirdPartySolutionRequest) {
        ThirdPartySolution duplicatedThirdPartySolution = thirdPartySolutionRepository.findByThirdPartySolutionName(thirdPartySolutionRequest.getThirdPartySolutionName());

        if (duplicatedThirdPartySolution != null) {
            throw new RoRoApiException(ErrorCode.THIRD_PARTY_SOLUTION_DUPLICATED_NAME, thirdPartySolutionRequest.getThirdPartySolutionName());
        }

        ThirdPartySolution thirdPartySolution = modelMapper.map(thirdPartySolutionRequest, ThirdPartySolution.class);
        thirdPartySolution.setDeleteYn(Domain101.N.name());
        thirdPartySolution.setRegistUserId(WebUtil.getUserId());
        thirdPartySolution.setRegistDatetime(new Date());
        thirdPartySolution.setModifyUserId(WebUtil.getUserId());
        thirdPartySolution.setModifyDatetime(new Date());

        thirdPartySolution = thirdPartySolutionRepository.save(thirdPartySolution);

        for (ThirdPartySearchTypeRequest thirdPartySearchTypeRequest : thirdPartySolutionRequest.getThirdPartySearchTypes()) {
            ThirdPartySearchType thirdPartySearchType = modelMapper.map(thirdPartySearchTypeRequest, ThirdPartySearchType.class);
            thirdPartySearchType.setThirdPartySolutionId(thirdPartySolution.getThirdPartySolutionId());
            thirdPartySearchType.setDeleteYn(Domain101.N.name());

            thirdPartySearchTypeRepository.save(thirdPartySearchType);
        }


        Map<String, Object> createThirdPartyMap = new HashMap<>();
        createThirdPartyMap.put("thirdPartySolutionId", thirdPartySolution.getThirdPartySolutionId());
        createThirdPartyMap.put("thirdPartySolutionName", thirdPartySolution.getThirdPartySolutionName());

        return createThirdPartyMap;

    }

    @Transactional
    public Map<String, Object> modifyThirdPartySolution(Long thirdPartySolutionId, ThirdPartySolutionRequest thirdPartySolutionRequest) {
        ThirdPartySolution thirdPartySolution = thirdPartySolutionRepository.findById(thirdPartySolutionId)
                .orElseThrow(() -> new ResourceNotFoundException("ThirdPartySolutionId(" + thirdPartySolutionId + ") does Not Found."));

        if (!thirdPartySolution.getThirdPartySolutionName().equals(thirdPartySolutionRequest.getThirdPartySolutionName())) {
            ThirdPartySolution duplicatedThirdPartySolution = thirdPartySolutionRepository.findByThirdPartySolutionName(thirdPartySolutionRequest.getThirdPartySolutionName());

            if (duplicatedThirdPartySolution != null) {
                throw new RoRoApiException(ErrorCode.THIRD_PARTY_SOLUTION_DUPLICATED_NAME, thirdPartySolutionRequest.getThirdPartySolutionName());
            }
        }

        thirdPartySolution.setThirdPartySolutionName(thirdPartySolutionRequest.getThirdPartySolutionName());
        thirdPartySolution.setVendor(thirdPartySolutionRequest.getVendor());
        thirdPartySolution.setDescription(thirdPartySolutionRequest.getDescription());
        thirdPartySolution.setModifyUserId(WebUtil.getUserId());
        thirdPartySolution.setModifyDatetime(new Date());

        thirdPartySolutionRepository.save(thirdPartySolution);

        log.debug("{}", thirdPartySolution);

        // 기존에 있는 검색유형을 삭제한다.
        List<ThirdPartySearchType> thirdPartySearchTypes = thirdPartySearchTypeRepository.findByThirdPartySolutionId(thirdPartySolutionId);
        for (ThirdPartySearchType thirdPartySearchType : thirdPartySearchTypes) {
            thirdPartySearchType.setDeleteYn(Domain101.Y.name());
            thirdPartySearchTypeRepository.save(thirdPartySearchType);
        }

        for (ThirdPartySearchTypeRequest thirdPartySearchTypeRequest : thirdPartySolutionRequest.getThirdPartySearchTypes()) {
            log.debug("{}", thirdPartySearchTypeRequest);
            ThirdPartySearchType thirdPartySearchType = modelMapper.map(thirdPartySearchTypeRequest, ThirdPartySearchType.class);
            thirdPartySearchType.setThirdPartySolutionId(thirdPartySolution.getThirdPartySolutionId());
            thirdPartySearchType.setDeleteYn(Domain101.N.name());

            thirdPartySearchTypeRepository.save(thirdPartySearchType);
        }

        Map<String, Object> modifyThirdPartyMap = new HashMap<>();
        modifyThirdPartyMap.put("thirdPartySolutionId", thirdPartySolutionId);
        modifyThirdPartyMap.put("thirdPartySolutionName", thirdPartySolutionRequest.getThirdPartySolutionName());

        return modifyThirdPartyMap;

    }

    @Transactional
    public void removeThirdPartySolution(Long thirdPartySolutionId) {
        ThirdPartySolution thirdPartySolution = thirdPartySolutionRepository.findById(thirdPartySolutionId)
                .orElseThrow(() -> new ResourceNotFoundException("ThirdPartySolutionId(" + thirdPartySolutionId + ") does Not Found."));

        thirdPartySolution.setDeleteYn(Domain101.Y.name());
        thirdPartySolution.setModifyUserId(WebUtil.getUserId());
        thirdPartySolution.setModifyDatetime(new Date());

        thirdPartySolutionRepository.save(thirdPartySolution);
    }

    public ByteArrayOutputStream excelExport() {
        return thirdPartyExcelExporter.createExcelReport(getThirdPartySolutionList());
    }

    private List<ThirdPartySolutionResponse> getThirdPartySolutionList() {
        List<ThirdPartySolutionResponse> thirdPartySolutionList = new ArrayList<>();

        for (ThirdPartySolutionListResponse r : getThirdPartySolutions()) {
            thirdPartySolutionList.add(getThirdPartySolution(r.getThirdPartySolutionId()));
        }

        return thirdPartySolutionList;
    }

    private int getSearchTypeOrder(String searchType) {
        switch (searchType) {
            case "PROCESS":
                return 1;
            case "RUNUSER":
                return 2;
            case "PKG":
                return 3;
            case "SVC":
                return 4;
            case "CMD":
                return 5;
            case "PORT":
                return 6;
            case "SCHEDULE":
                return 7;
            default:
                return 0;
        }
    }
}