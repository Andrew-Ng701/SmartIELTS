package com.andrew.smartielts.listening.service.admin.impl;

import com.andrew.smartielts.listening.domain.pojo.ListeningMaterial;
import com.andrew.smartielts.listening.mapper.ListeningMaterialMapper;
import com.andrew.smartielts.listening.service.admin.ListeningMaterialService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListeningMaterialServiceImpl implements ListeningMaterialService {

    private final ListeningMaterialMapper listeningMaterialMapper;

    public ListeningMaterialServiceImpl(ListeningMaterialMapper listeningMaterialMapper) {
        this.listeningMaterialMapper = listeningMaterialMapper;
    }

    @Override
    @Transactional
    public ListeningMaterial createMaterial(ListeningMaterial material) {
        if (material == null) {
            throw new RuntimeException("Material is required");
        }
        if (material.getTestId() == null) {
            throw new RuntimeException("Test id is required");
        }
        if (material.getPartGroupId() == null) {
            throw new RuntimeException("Part group id is required");
        }

        normalize(material);
        material.setIsDeleted(0);
        listeningMaterialMapper.insertListeningMaterial(material);
        return material;
    }

    @Override
    @Transactional
    public ListeningMaterial updateMaterial(Long id, ListeningMaterial material) {
        ListeningMaterial existing = listeningMaterialMapper.findActiveById(id);
        if (existing == null) {
            throw new RuntimeException("Listening material not found");
        }
        if (material == null) {
            throw new RuntimeException("Request body is required");
        }

        existing.setPartGroupId(material.getPartGroupId());
        existing.setTitle(trimToNull(material.getTitle()));
        existing.setAudioUrl(trimToNull(material.getAudioUrl()));
        existing.setAudioObjectKey(trimToNull(material.getAudioObjectKey()));
        existing.setTranscriptText(trimToNull(material.getTranscriptText()));
        existing.setDisplayOrder(material.getDisplayOrder());
        listeningMaterialMapper.updateListeningMaterial(existing);
        return listeningMaterialMapper.findActiveById(id);
    }

    @Override
    public ListeningMaterial getActiveById(Long id) {
        if (id == null) {
            throw new RuntimeException("Id is required");
        }
        return listeningMaterialMapper.findActiveById(id);
    }

    @Override
    public ListeningMaterial getAnyById(Long id) {
        if (id == null) {
            throw new RuntimeException("Id is required");
        }
        return listeningMaterialMapper.findAnyById(id);
    }

    @Override
    public List<ListeningMaterial> listActiveByTestId(Long testId) {
        if (testId == null) {
            throw new RuntimeException("Test id is required");
        }
        return listeningMaterialMapper.findActiveByTestId(testId);
    }

    @Override
    public List<ListeningMaterial> listAnyByTestId(Long testId) {
        if (testId == null) {
            throw new RuntimeException("Test id is required");
        }
        return listeningMaterialMapper.findAnyByTestId(testId);
    }

    @Override
    public List<ListeningMaterial> listActiveByPartGroupId(Long partGroupId) {
        if (partGroupId == null) {
            throw new RuntimeException("Part group id is required");
        }
        return listeningMaterialMapper.findActiveByPartGroupId(partGroupId);
    }

    @Override
    public List<ListeningMaterial> listAnyByPartGroupId(Long partGroupId) {
        if (partGroupId == null) {
            throw new RuntimeException("Part group id is required");
        }
        return listeningMaterialMapper.findAnyByPartGroupId(partGroupId);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (id == null) {
            throw new RuntimeException("Id is required");
        }
        listeningMaterialMapper.softDeleteById(id);
    }

    @Override
    @Transactional
    public void restoreById(Long id) {
        if (id == null) {
            throw new RuntimeException("Id is required");
        }
        listeningMaterialMapper.restoreById(id);
    }

    @Override
    @Transactional
    public void deleteByTestId(Long testId) {
        if (testId == null) {
            throw new RuntimeException("Test id is required");
        }
        listeningMaterialMapper.softDeleteByTestId(testId);
    }

    @Override
    @Transactional
    public void restoreByTestId(Long testId) {
        if (testId == null) {
            throw new RuntimeException("Test id is required");
        }
        listeningMaterialMapper.restoreByTestId(testId);
    }

    @Override
    @Transactional
    public void deleteByPartGroupId(Long partGroupId) {
        if (partGroupId == null) {
            throw new RuntimeException("Part group id is required");
        }
        listeningMaterialMapper.softDeleteByPartGroupId(partGroupId);
    }

    @Override
    @Transactional
    public void restoreByPartGroupId(Long partGroupId) {
        if (partGroupId == null) {
            throw new RuntimeException("Part group id is required");
        }
        listeningMaterialMapper.restoreByPartGroupId(partGroupId);
    }

    private void normalize(ListeningMaterial material) {
        material.setTitle(trimToNull(material.getTitle()));
        material.setAudioUrl(trimToNull(material.getAudioUrl()));
        material.setAudioObjectKey(trimToNull(material.getAudioObjectKey()));
        material.setTranscriptText(trimToNull(material.getTranscriptText()));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}