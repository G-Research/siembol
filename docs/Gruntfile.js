module.exports = function(grunt) {

    // Project configuration.
    grunt.initConfig({

        pkg: grunt.file.readJSON('package.json'),

        // Shell commands.
        shell: {
            jekyllServe: {
                command: 'jekyll serve'
            },

            gruntRecessApp: {
                command: 'grunt recess:app'
            },
            
            gruntConcat: {
                command: 'grunt concat'
            }
        },

        // Watch task.
        watch: {
            files: [
                '*.html',
                '_includes/*.html',
                '_layouts/*.html',
                '_less/modules/*.less',
                '_less/*.less',
                '_js/*.js',
                '_config.yml',
                'index.html'
            ],
            
            tasks: ['shell:gruntConcat', 'shell:gruntRecessApp', 'shell:jekyllServe'],
            
            options: {
                interrupt: true,
                atBegin: true
            }
        },

        // Less lint and compile.
        recess: {
          options: {
                compile: true,
                compress: true 
          },

          app: {
            src: ['_less/main.less'],
            dest: './css/main.css'
          }
        },

        // Concat for the JS files.
        concat: {
            dist: {
                src: [
                    './_js/console-errors.js',
                    './_js/window-onload.js',
                    './_js/document-ready.js',
                    './_js/window-resize.js',
                    './_js/widgets-config.js'
                ],

              dest: './js/main.js',
            }
        }
    });
    
    // NPM Dependencies.
    grunt.loadNpmTasks('grunt-shell');
    grunt.loadNpmTasks('grunt-contrib-watch');
    grunt.loadNpmTasks('grunt-recess');
    grunt.loadNpmTasks('grunt-contrib-concat');
 
    // Tasks.
    grunt.registerTask('default', ['shell']);
    grunt.registerTask('app', ['recess:app']);
};