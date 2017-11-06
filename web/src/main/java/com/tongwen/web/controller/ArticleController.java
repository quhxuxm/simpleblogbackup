package com.tongwen.web.controller;

import com.tongwen.common.IConstant;
import com.tongwen.domain.Article;
import com.tongwen.domain.ArticleDetail;
import com.tongwen.domain.ArticleSummary;
import com.tongwen.domain.Author;
import com.tongwen.service.api.IAnthologyService;
import com.tongwen.service.api.IArticleService;
import com.tongwen.service.exception.ServiceException;
import com.tongwen.web.request.ArticleEditRequest;
import com.tongwen.web.response.ArticleBookmarkResponse;
import com.tongwen.web.response.ArticleEditResponse;
import com.tongwen.web.response.ArticlePraiseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/article")
public class ArticleController {
    private static Logger logger = LoggerFactory
            .getLogger(ArticleController.class);
    private final IArticleService articleService;
    private final IAnthologyService anthologyService;
    private final ServletContext servletContext;
    @Value("${article.title.maxLength}")
    private int titleMaxLength;
    @Value("${article.title.minLength}")
    private int titleMinLength;
    @Value("${article.content.maxLength}")
    private int contentMaxLength;
    @Value("${article.content.minLength}")
    private int contentMinLength;

    @Autowired
    public ArticleController(IArticleService articleService,
            IAnthologyService anthologyService, ServletContext servletContext) {
        this.articleService = articleService;
        this.anthologyService = anthologyService;
        this.servletContext = servletContext;
    }

    @GetMapping("/{articleId}/view")
    public ModelAndView view(
            @PathVariable("articleId")
                    Long articleId) {
        ModelAndView result = new ModelAndView("article");
        ArticleDetail articleDetail = null;
        try {
            articleDetail = this.articleService.viewDetail(articleId);
        } catch (ServiceException e) {
            logger.error(
                    "Fail to retrieve article for view because of exception.",
                    e);
        }
        result.addObject("article", articleDetail);
        return result;
    }

    @GetMapping(value = "/{articleId}/praise", produces = "application/json")
    @ResponseBody
    public ArticlePraiseResponse praise(
            @PathVariable("articleId")
                    Long articleId) {
        ArticlePraiseResponse praiseResponse = new ArticlePraiseResponse();
        Long praiseNumber = null;
        try {
            praiseNumber = this.articleService.praiseArticle(articleId);
        } catch (ServiceException e) {
            logger.error("Fail to praise article because of exception.", e);
            praiseResponse.setSuccess(false);
            return praiseResponse;
        }
        praiseResponse.setSuccess(true);
        praiseResponse.setPraiseNumber(praiseNumber);
        return praiseResponse;
    }

    @GetMapping(value = "/{articleId}/bookmark", produces = "application/json")
    @ResponseBody
    public ArticleBookmarkResponse bookmark(
            @PathVariable("articleId")
                    Long articleId) {
        ArticleBookmarkResponse bookmarkResponse = new ArticleBookmarkResponse();
        Long bookmarkNumber = null;
        try {
            bookmarkNumber = this.articleService.bookmarkArticle(articleId);
        } catch (ServiceException e) {
            bookmarkResponse.setSuccess(false);
            return bookmarkResponse;
        }
        bookmarkResponse.setSuccess(true);
        bookmarkResponse.setBookmarkNumber(bookmarkNumber);
        return bookmarkResponse;
    }

    @GetMapping("/write")
    public ModelAndView showWrite(HttpSession session) {
        ModelAndView result = new ModelAndView("article-editor");
        Author author = (Author) session.getAttribute(
                IConstant.ISessionAttributeName.AUTHENTICATED_AUTHOR);
        return result;
    }

    @PostMapping(value = "/write", consumes = { "application/json" },
            produces = { "application/json" })
    @ResponseBody
    public ArticleEditResponse write(
            @RequestBody
                    ArticleEditRequest articleEditRequest,
            HttpSession httpSession) {
        ArticleEditResponse response = new ArticleEditResponse();
        this.validateArticleEditRequest(response, articleEditRequest);
        if (!response.isSuccess()) {
            return response;
        }
        Article article = new Article();
        article.setContent(articleEditRequest.getContent());
        article.setSummary(articleEditRequest.getSummary());
        article.setTitle(articleEditRequest.getTitle());
        article.setAnthologyId(articleEditRequest.getAnthologyId());
        Author authorInSession = (Author) httpSession.getAttribute(
                IConstant.ISessionAttributeName.AUTHENTICATED_AUTHOR);
        try {
            String imageControllerPath =
                    this.servletContext.getContextPath() + "/dimage";
            this.articleService.create(article, authorInSession);
        } catch (Exception e) {
            response.setSuccess(false);
            response.getErrorCodes()
                    .add(ArticleEditResponse.ErrorCode.SYSTEM_ERROR);
            return response;
        }
        response.setArticleId(article.getId());
        response.setSuccess(true);
        return response;
    }

    @GetMapping({ "/{articleId}/update" })
    public ModelAndView showArticleEdit(
            @PathVariable(name = "articleId")
                    Long articleId, HttpSession session) {
        ModelAndView result = new ModelAndView("article-editor");
        try {
            Author authorInSession = (Author) session.getAttribute(
                    IConstant.ISessionAttributeName.AUTHENTICATED_AUTHOR);
            result.addObject("article", this.articleService.get(articleId));
        } catch (ServiceException e) {
            logger.error(
                    "Fail to retrieve article for update because of exception.",
                    e);
        }
        return result;
    }

    @PostMapping(value = "/{articleId}/update",
            consumes = { "application/json" },
            produces = { "application/json" })
    @ResponseBody
    public ArticleEditResponse update(
            @PathVariable(name = "articleId")
                    Long articleId,
            @RequestBody
                    ArticleEditRequest articleEditRequest,
            HttpSession httpSession) {
        ArticleEditResponse response = new ArticleEditResponse();
        this.validateArticleEditRequest(response, articleEditRequest);
        if (!response.isSuccess()) {
            return response;
        }
        Article article = new Article();
        article.setContent(articleEditRequest.getContent());
        article.setSummary(articleEditRequest.getSummary());
        article.setTitle(articleEditRequest.getTitle());
        article.setId(articleId);
        article.setAnthologyId(articleEditRequest.getAnthologyId());
        response.setArticleId(articleId);
        Author authorInSession = (Author) httpSession.getAttribute(
                IConstant.ISessionAttributeName.AUTHENTICATED_AUTHOR);
        try {
            String imageControllerPath =
                    this.servletContext.getContextPath() + "/dimage";
            this.articleService.update(article, authorInSession);
        } catch (Exception e) {
            response.setSuccess(false);
            response.getErrorCodes()
                    .add(ArticleEditResponse.ErrorCode.SYSTEM_ERROR);
            return response;
        }
        response.setSuccess(true);
        return response;
    }

    private void validateArticleEditRequest(ArticleEditResponse response,
            ArticleEditRequest articleEditRequest) {
        if (articleEditRequest.getTitle() == null
                || articleEditRequest.getTitle().trim().length() == 0) {
            response.getErrorCodes()
                    .add(ArticleEditResponse.ErrorCode.TITLE_IS_EMPTY);
        }
        if (articleEditRequest.getTitle().trim().length()
                > this.titleMaxLength) {
            response.getErrorCodes()
                    .add(ArticleEditResponse.ErrorCode.TITLE_TOO_LONG);
        }
        if (articleEditRequest.getTitle().trim().length()
                < this.titleMinLength) {
            response.getErrorCodes()
                    .add(ArticleEditResponse.ErrorCode.TITLE_TOO_SHORT);
        }
        String contentPlainText = this.articleService
                .extractArticleContentPlainText(
                        articleEditRequest.getContent());
        if (contentPlainText == null || contentPlainText.trim().length() == 0) {
            response.getErrorCodes()
                    .add(ArticleEditResponse.ErrorCode.CONTENT_IS_EMPTY);
        }
        if (contentPlainText.trim().length() > this.contentMaxLength) {
            response.getErrorCodes()
                    .add(ArticleEditResponse.ErrorCode.CONTENT_TOO_LONG);
        }
        if (contentPlainText.trim().length() < this.contentMinLength) {
            response.getErrorCodes()
                    .add(ArticleEditResponse.ErrorCode.CONTENT_TOO_SHORT);
        }
        if (!response.getErrorCodes().isEmpty()) {
            response.setSuccess(false);
        }
    }

    @GetMapping("/summariesCollection")
    public ModelAndView summariesCollection(
            @RequestParam(name = "start", defaultValue = "0", required = false)
                    int start,
            @RequestParam(name = "desc", defaultValue = "true",
                    required = false)
                    boolean isDesc) {
        ModelAndView result = new ModelAndView(
                "/fragment/article/summariesCollection");
        List<ArticleSummary> articleSummariesCollectionPage = null;
        try {
            articleSummariesCollectionPage = this.articleService
                    .getSummariesOrderByPublishDate(start, isDesc);
        } catch (ServiceException e) {
            logger.error(
                    "Fail to get article summaries collection because of exception.",
                    e);
        }
        int nextStart = start + articleSummariesCollectionPage.size();
        result.addObject("nextStart", nextStart);
        result.addObject("summariesCollectionPage",
                articleSummariesCollectionPage);
        return result;
    }
}