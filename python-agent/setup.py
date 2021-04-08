from setuptools import find_packages, setup

setup(
    name='furms',
    packages=find_packages(include=['furms']),
    version='1.0.0',
    description='FURMS protocol MOM API',
    author='Bixbit s.c.',
    license='BSD 2-Clause',
    install_requires=['pika'],
    setup_requires=['pytest-runner'],
    tests_require=['pytest==4.4.1'],
    test_suite='tests'
)