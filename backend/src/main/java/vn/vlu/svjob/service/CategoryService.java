package vn.vlu.svjob.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vlu.svjob.dto.CommonDtos.CategoryRequest;
import vn.vlu.svjob.dto.CommonDtos.CategoryResponse;
import vn.vlu.svjob.entity.JobCategory;
import vn.vlu.svjob.exception.ApiException;
import vn.vlu.svjob.repository.JobCategoryRepository;

import java.util.List;

/** Danh mục ngành nghề (FR-21). "Xóa" chỉ là ẩn để không làm hỏng các tin cũ đang dùng ngành đó. */
@Service
public class CategoryService {

    private final JobCategoryRepository categoryRepository;

    public CategoryService(JobCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponse> activeCategories() {
        return categoryRepository.findByActiveTrueOrderByNameAsc().stream().map(CategoryService::toResponse).toList();
    }

    public List<CategoryResponse> allCategories() {
        return categoryRepository.findAllByOrderByNameAsc().stream().map(CategoryService::toResponse).toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest req) {
        if (categoryRepository.existsByNameIgnoreCase(req.name().trim())) {
            throw ApiException.conflict("Ngành nghề đã tồn tại");
        }
        JobCategory c = new JobCategory();
        c.setName(req.name().trim());
        c.setDescription(req.description());
        return toResponse(categoryRepository.save(c));
    }

    @Transactional
    public CategoryResponse update(Integer id, CategoryRequest req) {
        JobCategory c = find(id);
        if (!c.getName().equalsIgnoreCase(req.name().trim()) && categoryRepository.existsByNameIgnoreCase(req.name().trim())) {
            throw ApiException.conflict("Ngành nghề đã tồn tại");
        }
        c.setName(req.name().trim());
        c.setDescription(req.description());
        if (req.active() != null) {
            c.setActive(req.active());
        }
        return toResponse(c);
    }

    @Transactional
    public void hide(Integer id) {
        find(id).setActive(false);
    }

    JobCategory findActive(Integer id) {
        JobCategory c = find(id);
        if (!c.isActive()) {
            throw ApiException.badRequest("Ngành nghề này đã bị ẩn");
        }
        return c;
    }

    private JobCategory find(Integer id) {
        return categoryRepository.findById(id).orElseThrow(() -> ApiException.notFound("Không tìm thấy ngành nghề"));
    }

    private static CategoryResponse toResponse(JobCategory c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getDescription(), c.isActive());
    }
}
