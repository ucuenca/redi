//Configuration file for the tasks that Gulp does for the files of REDI
var gulp = require('gulp'),
    sass = require('gulp-ruby-sass'),
    autoprefixer = require('gulp-autoprefixer'),
    cssnano = require('gulp-cssnano'),
    jshint = require('gulp-jshint'),
    uglify = require('gulp-uglify'),
    imagemin = require('gulp-imagemin'),
    rename = require('gulp-rename'),
    concat = require('gulp-concat'),
    notify = require('gulp-notify'),
    cache = require('gulp-cache'),
    livereload = require('gulp-livereload'),
    del = require('del');

// Styles
gulp.task('styles', function() {
  return gulp.src(['wkhome/css/**/*.css', '!wkhome/css/**/*.min.css'])
    .pipe(autoprefixer('last 2 version'))
    //.pipe(gulp.dest('dist/styles'))
    .pipe(rename({ suffix: '.min' }))
    .pipe(cssnano())
    .pipe(gulp.dest('wkhome/cssmin/'))
    //.pipe(notify({ message: 'Styles task complete' }));
});

// Scripts
gulp.task('scripts', function() {
  return gulp.src(['wkhome/js/**/*.js', '!wkhome/js/**/*.min.*'])
    //.pipe(jshint('.jshintrc'))
    //.pipe(jshint.reporter('default'))
    //.pipe(concat('main.js'))
    //.pipe(gulp.dest('dist/scripts'))
    .pipe(rename({ suffix: '.min' }))
    .pipe(uglify())
    .pipe(gulp.dest('wkhome/jsmin/'));
    //.pipe(notify({ message: 'Scripts task complete' }));
});

// Images: it is not included in the default task now
gulp.task('images', function() {
  return gulp.src('wkhome/images/*')
    .pipe(imagemin({ optimizationLevel: 3, progressive: true, interlaced: true }))
    .pipe(gulp.dest('dist/images/*'));
    //.pipe(notify({ message: 'Images task complete' }));
});

// Clean
gulp.task('clean', function() {
  return del(['wkhome/jsmin/**/*.min.js', 'wkhome/cssmin/**/*.min.css']);
});

// Default task
gulp.task('default', ['clean'], function() {
    gulp.start('styles', 'scripts');
});

// Watch
gulp.task('watch', function() {

  // Watch .scss files
  gulp.watch(['wkhome/css/**/*.css', '!wkhome/css/**/*.min.*'], ['clean', 'styles']);

  // Watch .js files
  gulp.watch(['wkhome/js/**/*.js', '!wkhome/js/**/*.min.*'], ['clean', 'scripts']);

  // Watch image files
  //gulp.watch('wkhome/images/*', ['images']);

  // Create LiveReload server
  //livereload.listen();

  // Watch any files in dist/, reload on change
  //gulp.watch(['dist/**']).on('change', livereload.changed);

});



