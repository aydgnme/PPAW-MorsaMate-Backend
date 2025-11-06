package me.aydgn.MorseMate.controller.admin;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Base helpers shared by admin view controllers to keep the individual
 * controllers focused on orchestration logic instead of boilerplate.
 */
abstract class AbstractAdminPageController {

    protected void setActivePage(Model model, AdminPage page) {
        model.addAttribute("activePage", page.getNavigationKey());
    }

    protected String render(Model model, AdminPage page) {
        setActivePage(model, page);
        return page.getViewName();
    }

    protected Long resolveEditingId(Long requestedId, Model model) {
        if (requestedId != null) {
            return requestedId;
        }
        Object attribute = model.asMap().get("editingId");
        if (attribute instanceof Long attrLong) {
            return attrLong;
        }
        return null;
    }

    protected void preserveFormState(String attributeName,
                                     Object form,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(
                "org.springframework.validation.BindingResult." + attributeName,
                bindingResult
        );
        redirectAttributes.addFlashAttribute(attributeName, form);
    }

    protected void preserveEditingId(Long editingId, RedirectAttributes redirectAttributes) {
        if (editingId != null) {
            redirectAttributes.addFlashAttribute("editingId", editingId);
        }
    }
}
