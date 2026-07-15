package com.skillpath.service;

import com.skillpath.dto.request.AddPortfolioItemRequest;
import com.skillpath.dto.response.PortfolioItemResponse;
import com.skillpath.exception.ResourceNotFoundException;
import com.skillpath.model.PortfolioItem.PortfolioItem;
import com.skillpath.repository.PortfolioItemRepository;
import com.skillpath.repository.ProjectRepository;
import com.skillpath.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioItemRepository portfolioRepo;
    private final ProjectRepository       projectRepo;
    private final UserRepository          userRepo;

    public List<PortfolioItemResponse> getPortfolio(Long userId) {
        if (!userRepo.existsById(userId))
            throw new ResourceNotFoundException("User not found: " + userId);

        return portfolioRepo.findByUserId(userId)
                .stream()
                .map(item -> {
                    String projectName = null;
                    if (item.getProjectId() != null) {
                        projectName = projectRepo.findById(item.getProjectId())
                                .map(p -> p.getName())
                                .orElse(null);
                    }
                    return PortfolioItemResponse.from(item, projectName);
                })
                .toList();
    }

    @Transactional
    public PortfolioItemResponse addItem(Long userId, AddPortfolioItemRequest req) {
        if (!userRepo.existsById(userId))
            throw new ResourceNotFoundException("User not found: " + userId);

        // If a projectId is provided, verify it actually exists
        if (req.getProjectId() != null && !projectRepo.existsById(req.getProjectId()))
            throw new ResourceNotFoundException("Project not found: " + req.getProjectId());

        PortfolioItem item = PortfolioItem.builder()
                .userId(userId)
                .projectId(req.getProjectId())
                .githubUrl(req.getGithubUrl())
                .description(req.getDescription())
                .userRole(req.getUserRole())
                .build();

        PortfolioItem saved = portfolioRepo.save(item);

        String projectName = null;
        if (saved.getProjectId() != null) {
            projectName = projectRepo.findById(saved.getProjectId())
                    .map(p -> p.getName())
                    .orElse(null);
        }

        return PortfolioItemResponse.from(saved, projectName);
    }

    
    @Transactional
    public void deleteItem(Long userId, Long itemId) {
        PortfolioItem item = portfolioRepo.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException( "Portfolio item not found: " + itemId));
        if (!item.getUserId().equals(userId))
            throw new SecurityException(
                    "User " + userId + " does not own portfolio item " + itemId);

        portfolioRepo.delete(item);
    }

    @Transactional
    public void autoGenerateForCompletedProject(Long projectId, Long userId, String userRole) {
        boolean alreadyExists = portfolioRepo.findByUserId(userId).stream()
                                        .anyMatch(item -> projectId.equals(item.getProjectId()));

        if (alreadyExists) return;

        PortfolioItem item = PortfolioItem.builder()
                .userId(userId)
                .projectId(projectId)
                .userRole(userRole)
                .description("Auto-generated from completed project")
                .build();

        portfolioRepo.save(item);
    }
}