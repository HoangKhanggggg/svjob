package vn.vlu.svjob.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
