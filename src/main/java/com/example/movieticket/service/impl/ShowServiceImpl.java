package com.example.movieticket.service.impl;

import com.example.movieticket.common.UserRole;
import com.example.movieticket.dto.request.ShowRequest;
import com.example.movieticket.dto.response.PageResponse;
import com.example.movieticket.dto.response.ShowResponse;
import com.example.movieticket.exception.AppException;
import com.example.movieticket.exception.ErrorCode;
import com.example.movieticket.model.Show;
import com.example.movieticket.repository.MovieRepository;
import com.example.movieticket.repository.ScreenRepository;
import com.example.movieticket.repository.ShowRepository;
import com.example.movieticket.service.ShowService;
import com.example.movieticket.service.UserService;
import com.example.movieticket.util.PaginationUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShowServiceImpl implements ShowService {
    ShowRepository showRepository;
    MovieRepository movieRepository;
    ScreenRepository screenRepository;
    private final UserService userService;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ShowResponse createShow(ShowRequest request) {
        var movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));
        var screen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new AppException(ErrorCode.SCREEN_NOT_FOUND));

        if (request.getShowDate().isBefore(LocalDate.now())) {
            throw new AppException(ErrorCode.INVALID_SHOW_DATE);
        }

        if (request.getStartTime().isAfter(request.getEndTime()) || request.getStartTime().equals(request.getEndTime())) {
            throw new AppException(ErrorCode.INVALID_SHOW_TIME);
        }

        long duration = Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
        if (duration < movie.getDurationMinutes()) {
            throw new AppException(ErrorCode.INVALID_DURATION);
        }

        boolean isConflict = showRepository.existsByScreenAndShowDateAndTimeOverlap(
                screen, request.getShowDate(), request.getStartTime(), request.getEndTime());
        if (isConflict) {
            throw new AppException(ErrorCode.SHOWTIME_CONFLICT);
        }

        if (request.getShowDate().isBefore(movie.getReleaseDate())) {
            throw new AppException(ErrorCode.INVALID_SHOW_DATE);
        }

        var show = Show.builder()
                .movie(movie)
                .screen(screen)
                .showDate(request.getShowDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .active(true)
                .build();

        return makeShowResponse(showRepository.save(show));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ShowResponse updateShow(Integer showId, ShowRequest request) {
        var movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));
        var screen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new AppException(ErrorCode.SCREEN_NOT_FOUND));

        var show = showRepository.findById(showId)
                .orElseThrow(()-> new AppException(ErrorCode.SHOW_NOT_FOUND));
        if(request.getMovieId() != null) show.setMovie(movie);
        if(request.getScreenId() != null) show.setScreen(screen);
        if(request.getShowDate() != null) show.setShowDate(request.getShowDate());
        if(request.getStartTime() != null) show.setStartTime(request.getStartTime());
        if(request.getEndTime() != null) show.setEndTime(request.getEndTime());
        if(request.getActive() != null) show.setActive(request.getActive());

        return makeShowResponse(showRepository.save(show));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ShowResponse changeActive(Integer showId, Boolean active) {
        var show = showRepository.findById(showId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOW_NOT_FOUND));
        show.setActive(active);
        return makeShowResponse(showRepository.save(show));
    }

    @Override
    public PageResponse<ShowResponse> getShowByMovie(Integer movieId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        var movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));
        Page<Show> shows;
        var user = userService.getCurrentUser();
        if(user != null && user.getRole().equals(UserRole.ADMIN)){
            shows = showRepository.findByMovie(movie, pageable);
        }else {
            shows = showRepository.findByMovieAndActiveTrue(movie, pageable);
        }
        return PaginationUtil.mapToPageResponse(shows, this::makeShowResponse);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<ShowResponse> getAllShow(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Show> shows = showRepository.findAll(pageable);

        return PaginationUtil.mapToPageResponse(shows, this::makeShowResponse);
    }

    @Override
    public ShowResponse getShowById(Integer showId) {
        var show = showRepository.findById(showId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOW_NOT_FOUND));
        return makeShowResponse(show);
    }

    private ShowResponse makeShowResponse(Show show) {
        return ShowResponse.builder()
                .showId(show.getId())
                .movieId(show.getMovie().getId())
                .screenId(show.getScreen().getId())
                .showDate(show.getShowDate())
                .startTime(show.getStartTime())
                .endTime(show.getEndTime())
                .createdAt(show.getCreatedAt())
                .updatedAt(show.getUpdatedAt())
                .active(show.isActive())
                .build();
    }
}
